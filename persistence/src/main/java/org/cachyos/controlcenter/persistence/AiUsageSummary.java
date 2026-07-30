package org.cachyos.controlcenter.persistence;

/** Locally recorded Responses API usage for the current calendar month. */
public record AiUsageSummary(
    long inputTokens, long outputTokens, long estimatedMillicents, long requests) {
  public double estimatedUsd() {
    return estimatedMillicents / 100_000.0;
  }

  public boolean belowBudget(int budgetCents) {
    return budgetCents > 0 && estimatedMillicents < budgetCents * 1_000L;
  }
}
