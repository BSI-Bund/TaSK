package com.achelos.task.restimpl.models;

import java.util.Objects;

import jakarta.validation.Valid;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import io.swagger.v3.oas.annotations.media.Schema;


@JsonTypeName("RunId")
public class RunId {
  private @Valid String runId;

  public RunId(final String runId) {
    this.runId = runId;
  }


  @Schema
  @JsonProperty("runId")
  public String getRunId() {
    return runId;
  }

  @JsonProperty("runId")
  public void setRunId(String runId) {
    this.runId = runId;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RunId runId = (RunId) o;
    return Objects.equals(this.runId, runId.runId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(runId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RunId {\n");
    
    sb.append("    runId: ").append(toIndentedString(runId)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }


}

