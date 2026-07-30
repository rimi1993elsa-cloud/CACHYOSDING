package org.cachyos.controlcenter.ai.knowledge;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/** Bounded HTTPS fetcher for registry-owned sources only. */
public final class HttpKnowledgeFetcher implements KnowledgeFetcher {
  private static final int MAXIMUM_BODY_BYTES = 1_000_000;
  private final HttpClient client;

  public HttpKnowledgeFetcher() {
    client =
        HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();
  }

  @Override
  public KnowledgeDocument fetch(KnowledgeSource source) throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder(source.uri())
            .timeout(Duration.ofSeconds(20))
            .header("Accept", "text/html")
            .header("User-Agent", "CachyOS-Control-Center/0.1 documentation-cache")
            .GET()
            .build();
    HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
    if (response.statusCode() != 200 || response.body().length > MAXIMUM_BODY_BYTES) {
      throw new IOException("Documentation source unavailable");
    }
    Document document =
        Jsoup.parse(new String(response.body(), java.nio.charset.StandardCharsets.UTF_8));
    document.select("script,style,nav,footer,header,form,noscript").remove();
    String text = DocumentSafety.sanitize(document.body().text());
    return new KnowledgeDocument(source.id(), source.title(), source.uri(), Instant.now(), text);
  }
}
