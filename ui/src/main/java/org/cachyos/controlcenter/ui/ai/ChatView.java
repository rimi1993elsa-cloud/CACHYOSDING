package org.cachyos.controlcenter.ui.ai;

import java.util.ArrayList;
import java.util.List;
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
import org.cachyos.controlcenter.ai.provider.AiConfiguration;

/** Text-only advisory chat. This class intentionally has no action dispatcher dependency. */
public final class ChatView extends VBox {
  private static final int MAXIMUM_HISTORY_MESSAGES = 20;
  private final AiProvider provider;
  private final AiConfiguration configuration;
  private final List<AiMessage> history = new ArrayList<>();
  private final TextArea conversation = new TextArea();
  private final TextArea question = new TextArea();
  private final Button send = new Button("Frage senden");
  private final Button cancel = new Button("Abbrechen");
  private final Label status = new Label();
  private StringBuilder currentAnswer;

  public ChatView(AiProvider provider, AiConfiguration configuration) {
    this.provider = provider;
    this.configuration = configuration;
    conversation.setId("chat-conversation");
    conversation.setEditable(false);
    conversation.setWrapText(true);
    VBox.setVgrow(conversation, Priority.ALWAYS);
    question.setId("chat-question");
    question.setPromptText("Frage zu CachyOS oder Linux");
    question.setWrapText(true);
    question.setPrefRowCount(3);
    send.setId("chat-send");
    send.setDisable(!provider.available());
    send.setOnAction(ignored -> send());
    cancel.setId("chat-cancel");
    cancel.setDisable(true);
    cancel.setOnAction(
        ignored -> {
          provider.cancel();
          cancel.setDisable(true);
          send.setDisable(!provider.available());
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
    HBox actions = new HBox(8, send, cancel);
    setSpacing(10);
    setPadding(new Insets(2));
    getChildren()
        .addAll(
            model,
            pricing,
            status,
            conversation,
            new Label("Deine Frage"),
            question,
            actions,
            new Label(
                "Die KI antwortet nur mit Text. Sie kann keine lokale Aktion ausführen oder bestätigen."));
  }

  public void setDraft(String text) {
    question.setText(text == null ? "" : text);
    question.requestFocus();
  }

  private void send() {
    String text = question.getText().strip();
    if (text.isBlank() || !provider.available()) {
      return;
    }
    List<AiMessage> requestHistory = List.copyOf(history);
    append("Du", text);
    history.add(new AiMessage(AiMessage.Role.USER, text));
    trimHistory();
    question.clear();
    currentAnswer = new StringBuilder();
    send.setDisable(true);
    cancel.setDisable(false);
    status.setText("Online-Anfrage läuft …");
    provider.stream(new AiRequest(text, requestHistory, ""), this::handle)
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
    send.setDisable(!provider.available());
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
}
