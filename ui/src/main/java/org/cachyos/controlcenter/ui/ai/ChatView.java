package org.cachyos.controlcenter.ui.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.cachyos.controlcenter.ai.api.AiMessage;
import org.cachyos.controlcenter.ai.api.AiProvider;
import org.cachyos.controlcenter.ai.api.AiRequest;
import org.cachyos.controlcenter.ai.api.AiStreamEvent;
import org.cachyos.controlcenter.ai.knowledge.KnowledgeMatch;
import org.cachyos.controlcenter.ai.knowledge.KnowledgeService;
import org.cachyos.controlcenter.ai.provider.AiConfiguration;
import org.cachyos.controlcenter.persistence.SettingsService;

/** Text-only advisory chat. This class intentionally has no action dispatcher dependency. */
public final class ChatView extends VBox {
  private static final int MAXIMUM_HISTORY_MESSAGES = 20;
  private final AiProvider provider;
  private final AiConfiguration configuration;
  private final KnowledgeService knowledgeService;
  private final SettingsService settings;
  private final String systemContext;
  private final String hardwareContext;
  private final List<AiMessage> history = new ArrayList<>();
  private final TextArea conversation = new TextArea();
  private final TextArea question = new TextArea();
  private final Button send = new Button("Frage senden");
  private final Button cancel = new Button("Abbrechen");
  private final Label status = new Label();
  private final TextArea sources = new TextArea();
  private StringBuilder currentAnswer;

  public ChatView(
      AiProvider provider,
      AiConfiguration configuration,
      KnowledgeService knowledgeService,
      SettingsService settings,
      String systemContext,
      String hardwareContext) {
    this.provider = provider;
    this.configuration = configuration;
    this.knowledgeService = knowledgeService;
    this.settings = settings;
    this.systemContext = systemContext;
    this.hardwareContext = hardwareContext;
    conversation.setId("chat-conversation");
    conversation.setEditable(false);
    conversation.setWrapText(true);
    VBox.setVgrow(conversation, Priority.ALWAYS);
    question.setId("chat-question");
    question.setPromptText("Frage zu CachyOS oder Linux");
    question.setWrapText(true);
    question.setPrefRowCount(3);
    sources.setId("chat-sources");
    sources.setEditable(false);
    sources.setWrapText(true);
    sources.setPrefRowCount(3);
    sources.setPromptText("Noch keine lokalen Quellen ausgewählt.");
    send.setId("chat-send");
    send.setDisable(!onlineAllowed());
    send.setOnAction(ignored -> send());
    cancel.setId("chat-cancel");
    cancel.setDisable(true);
    cancel.setOnAction(
        ignored -> {
          provider.cancel();
          cancel.setDisable(true);
          send.setDisable(!onlineAllowed());
          status.setText("Anfrage abgebrochen.");
        });
    status.setText(provider.availabilityMessage());
    status.getStyleClass().add("muted-label");

    Label model =
        new Label(
            "Modell: "
                + configuration.model()
                + " · Ausgabegrenze: "
                + configuration.maximumOutputTokens()
                + " Tokens");
    model.getStyleClass().add("muted-label");
    Hyperlink pricing = new Hyperlink("API-Nutzung kann Kosten verursachen · Preise prüfen");
    pricing.setOnAction(ignored -> status.setText("Preise: https://openai.com/api/pricing/"));
    Button refreshKnowledge = new Button("Offizielle Quellen aktualisieren");
    refreshKnowledge.setId("knowledge-refresh");
    refreshKnowledge.setOnAction(
        ignored -> {
          refreshKnowledge.setDisable(true);
          status.setText("Offizielle Wissensquellen werden aktualisiert …");
          knowledgeService
              .refreshStale()
              .whenComplete(
                  (updated, error) ->
                      Platform.runLater(
                          () -> {
                            refreshKnowledge.setDisable(false);
                            if (error != null) {
                              status.setText("Quellenaktualisierung fehlgeschlagen.");
                            } else {
                              status.setText(
                                  updated
                                      + " Quelle(n) aktualisiert · "
                                      + knowledgeService.documentCount()
                                      + " im Cache.");
                            }
                          }));
        });
    HBox actions = new HBox(8, send, cancel, refreshKnowledge);
    setSpacing(10);
    setPadding(new Insets(2));
    getChildren()
        .addAll(
            model,
            pricing,
            status,
            conversation,
            new Label("Lokale Wissensquellen"),
            sources,
            new Label("Deine Frage"),
            question,
            actions,
            new Label(
                "Die KI antwortet nur mit Text. Sie kann keine lokale Aktion ausführen oder bestätigen."));
    restoreHistory();
  }

  public void setDraft(String text) {
    question.setText(text == null ? "" : text);
    question.requestFocus();
  }

  public void clearHistory() {
    history.clear();
    conversation.clear();
  }

  public void applySettings() {
    send.setDisable(!onlineAllowed());
    status.setText(
        onlineAllowed()
            ? provider.availabilityMessage()
            : "Online-KI ist durch Anbieterwahl, Budget oder Datenschutz deaktiviert.");
  }

  private void send() {
    String text = question.getText().strip();
    if (text.isBlank() || !onlineAllowed()) {
      applySettings();
      return;
    }
    List<AiMessage> requestHistory =
        settings.current().storeChatHistory() ? List.copyOf(history) : List.of();
    List<KnowledgeMatch> matches =
        settings.current().shareDocumentation() ? knowledgeService.search(text, 3) : List.of();
    updateSources(matches);
    append("Du", text);
    history.add(new AiMessage(AiMessage.Role.USER, text));
    settings.recordChat("user", text);
    trimHistory();
    question.clear();
    currentAnswer = new StringBuilder();
    send.setDisable(true);
    cancel.setDisable(false);
    status.setText("Online-Anfrage läuft …");
    provider.stream(new AiRequest(text, requestHistory, approvedContext(matches)), this::handle)
        .exceptionally(
            ignored -> {
              return null;
            });
  }

  private void handle(AiStreamEvent event) {
    Platform.runLater(
        () -> {
          switch (event.state()) {
            case DELTA -> {
              currentAnswer.append(event.text());
              conversation.appendText(event.text());
            }
            case COMPLETED -> {
              if (!currentAnswer.isEmpty()) {
                history.add(new AiMessage(AiMessage.Role.ASSISTANT, currentAnswer.toString()));
                settings.recordChat("assistant", currentAnswer.toString());
                trimHistory();
              }
              conversation.appendText(System.lineSeparator());
              status.setText("Antwort vollständig.");
              finish();
            }
            case ERROR -> {
              append("Hinweis", event.text());
              status.setText(event.text());
              finish();
            }
            default -> {
              status.setText("Unbekannter Streamingzustand.");
              finish();
            }
          }
        });
  }

  private void finish() {
    cancel.setDisable(true);
    send.setDisable(!onlineAllowed());
  }

  private void append(String author, String text) {
    conversation.appendText(
        (conversation.getText().isBlank() ? "" : System.lineSeparator())
            + author
            + ": "
            + text
            + System.lineSeparator()
            + ("Assistent".equals(author) ? "" : "Assistent: "));
  }

  private void trimHistory() {
    while (history.size() > MAXIMUM_HISTORY_MESSAGES) {
      history.removeFirst();
    }
  }

  private void updateSources(List<KnowledgeMatch> matches) {
    if (matches.isEmpty()) {
      sources.setText(
          knowledgeService.documentCount() == 0
              ? "Kein Cache verfügbar; die Frage wird ohne Dokumentkontext gestellt."
              : "Keine passende offizielle Quelle im lokalen Cache gefunden.");
      return;
    }
    sources.setText(
        matches.stream()
            .map(match -> match.title() + " · " + match.uri() + " · Stand " + match.fetchedAt())
            .collect(Collectors.joining(System.lineSeparator())));
  }

  private static String buildContext(List<KnowledgeMatch> matches) {
    return matches.stream()
        .map(
            match ->
                "QUELLE: "
                    + match.title()
                    + "\nURL: "
                    + match.uri()
                    + "\nABRUF: "
                    + match.fetchedAt()
                    + "\nAUSZUG (UNTRUSTED DATA):\n"
                    + match.excerpt())
        .collect(Collectors.joining("\n\n---\n\n"));
  }

  private String approvedContext(List<KnowledgeMatch> matches) {
    List<String> approved = new ArrayList<>();
    if (settings.current().shareDocumentation()) {
      approved.add(buildContext(matches));
    }
    if (settings.current().shareSystemContext() && !systemContext.isBlank()) {
      approved.add("FREIGEGEBENE SYSTEMBASISDATEN:\n" + systemContext);
    }
    if (settings.current().shareHardware() && !hardwareContext.isBlank()) {
      approved.add("FREIGEGEBENE ANONYMISIERTE HARDWAREDATEN:\n" + hardwareContext);
    }
    return approved.stream().filter(value -> !value.isBlank()).collect(Collectors.joining("\n\n"));
  }

  private boolean onlineAllowed() {
    return provider.available()
        && settings.current().onlineAiEnabled()
        && settings.current().monthlyBudgetCents() > 0
        && "openai".equals(settings.current().aiProvider());
  }

  private void restoreHistory() {
    if (!settings.current().storeChatHistory()) {
      return;
    }
    settings
        .history()
        .forEach(
            entry -> {
              AiMessage.Role role =
                  "user".equals(entry.role()) ? AiMessage.Role.USER : AiMessage.Role.ASSISTANT;
              history.add(new AiMessage(role, entry.text()));
              conversation.appendText(
                  ("user".equals(entry.role()) ? "Du: " : "Assistent: ")
                      + entry.text()
                      + System.lineSeparator());
            });
    trimHistory();
  }
}
