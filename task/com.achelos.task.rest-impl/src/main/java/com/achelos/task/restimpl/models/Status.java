package com.achelos.task.restimpl.models;

import java.util.Objects;

import jakarta.validation.Valid;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;

import io.swagger.v3.oas.annotations.media.Schema;


@JsonTypeName("Status")
@Schema
public class Status {
    public enum StatusEnum {

        SCHEDULED(String.valueOf("Scheduled")), RUNNING(String.valueOf("Running")), ABORTED(String.valueOf("Aborted")), EXECUTED(String.valueOf("Executed"));


        private String value;

        StatusEnum (String v) {
            value = v;
        }

        public String value() {
            return value;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }

        /**
         * Convert a String into String, as specified in the
         * <a href="https://download.oracle.com/otndocs/jcp/jaxrs-2_0-fr-eval-spec/index.html">See JAX RS 2.0 Specification, section 3.2, p. 12</a>
         */
        public static StatusEnum fromString(String s) {
            for (StatusEnum b : StatusEnum.values()) {
                // using Objects.toString() to be safe if value type non-object type
                // because types like 'int' etc. will be auto-boxed
                if (Objects.toString(b.value).equals(s)) {
                    return b;
                }
            }
            throw new IllegalArgumentException("Unexpected string value '" + s + "'");
        }

        @JsonCreator
        public static StatusEnum fromValue(String value) {
            for (StatusEnum b : StatusEnum.values()) {
                if (b.value.equals(value)) {
                    return b;
                }
            }
            throw new IllegalArgumentException("Unexpected value '" + value + "'");
        }
    }

    private @Valid StatusEnum status;
    private @Valid String downloadURI;

    public Status(final StatusEnum statusEnum) {
        this.status = statusEnum;
        this.downloadURI = null;
    }

    public Status(final StatusEnum statusEnum, final String downloadURI) {
        this.status = statusEnum;
        this.downloadURI = downloadURI;
    }
    /**
     **/
    public Status status(StatusEnum status) {
        this.status = status;
        return this;
    }


    @Schema
    @JsonProperty("status")
    public StatusEnum getStatus() {
        return status;
    }

    @JsonProperty("status")
    public void setStatus(StatusEnum status) {
        this.status = status;
    }

    /**
     **/
    public Status downloadURI(String downloadURI) {
        this.downloadURI = downloadURI;
        return this;
    }


    @Schema
    @JsonProperty("downloadURI")
    public String getDownloadURI() {
        return downloadURI;
    }

    @JsonProperty("downloadURI")
    public void setDownloadURI(String downloadURI) {
        this.downloadURI = downloadURI;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Status status = (Status) o;
        return Objects.equals(this.status, status.status) &&
                Objects.equals(this.downloadURI, status.downloadURI);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, downloadURI);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Status {\n");

        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    downloadURI: ").append(toIndentedString(downloadURI)).append("\n");
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

