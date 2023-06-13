package com.achelos.task.restimpl.models;

import java.util.Objects;

import jakarta.validation.Valid;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import io.swagger.v3.oas.annotations.media.Schema;



@JsonTypeName("ErrorResponse")
@Schema
public class ErrorResponse {
  private @Valid String timestamp;
  private @Valid String statuscode;
  private @Valid String errormessage;
  private @Valid String path;

  public ErrorResponse(final String timestamp, final String statuscode, final String errormessage, final String path){
    this.timestamp = timestamp;
    this.statuscode = statuscode;
    this.errormessage = errormessage;
    this.path = path;
  }
  /**
   **/
  public ErrorResponse timestamp(String timestamp) {
    this.timestamp = timestamp;
    return this;
  }


  @Schema
  @JsonProperty("timestamp")
  public String getTimestamp() {
    return timestamp;
  }

  @JsonProperty("timestamp")
  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }

  /**
   **/
  public ErrorResponse statuscode(String statuscode) {
    this.statuscode = statuscode;
    return this;
  }


  @Schema
  @JsonProperty("statuscode")
  public String getStatuscode() {
    return statuscode;
  }

  @JsonProperty("statuscode")
  public void setStatuscode(String statuscode) {
    this.statuscode = statuscode;
  }

  /**
   **/
  public ErrorResponse errormessage(String errormessage) {
    this.errormessage = errormessage;
    return this;
  }


  @Schema
  @JsonProperty("errormessage")
  public String getErrormessage() {
    return errormessage;
  }

  @JsonProperty("errormessage")
  public void setErrormessage(String errormessage) {
    this.errormessage = errormessage;
  }

  /**
   **/
  public ErrorResponse path(String path) {
    this.path = path;
    return this;
  }


  @Schema
  @JsonProperty("path")
  public String getPath() {
    return path;
  }

  @JsonProperty("path")
  public void setPath(String path) {
    this.path = path;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ErrorResponse errorResponse = (ErrorResponse) o;
    return Objects.equals(this.timestamp, errorResponse.timestamp) &&
        Objects.equals(this.statuscode, errorResponse.statuscode) &&
        Objects.equals(this.errormessage, errorResponse.errormessage) &&
        Objects.equals(this.path, errorResponse.path);
  }

  @Override
  public int hashCode() {
    return Objects.hash(timestamp, statuscode, errormessage, path);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ErrorResponse {\n");
    
    sb.append("    timestamp: ").append(toIndentedString(timestamp)).append("\n");
    sb.append("    statuscode: ").append(toIndentedString(statuscode)).append("\n");
    sb.append("    errormessage: ").append(toIndentedString(errormessage)).append("\n");
    sb.append("    path: ").append(toIndentedString(path)).append("\n");
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

