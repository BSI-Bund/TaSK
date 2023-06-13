package com.achelos.task.restimpl.api;

import com.achelos.task.restimpl.models.ErrorResponse;
import com.achelos.task.utilities.DateTimeUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@Path("")
@Tag(name= "Base Path", description = "Get basic information on this endpoint.")
public class DefaultApi {

    private final static String OPENAPI_YAML_RESOURCE_PATH = "openapi.yaml";

    @GET
    @Produces({ "text/yaml" , MediaType.APPLICATION_JSON})
    @Operation(summary = "Return the API definition of the REST Server.", description = "Return the API definition of the REST Server.", tags={ "Base Path" })
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation= String.class))),
        @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(
        		mediaType = MediaType.APPLICATION_JSON,
        		schema = @Schema(implementation= ErrorResponse.class))) })
    public Response getAPIDefinition() {
        try (var inputStream = getClass().getClassLoader().getResourceAsStream(OPENAPI_YAML_RESOURCE_PATH)) {
            var openApiDefinition = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            var response = Response.ok(openApiDefinition);
            response.header("Content-Disposition", "attachment; filename=" + OPENAPI_YAML_RESOURCE_PATH);
            return response.build();
        } catch (Exception e) {
            var errorResponse = generateErrorResponse("Internal Server Error: " + e.getMessage());
            var response = Response.serverError().entity(errorResponse);
            response.type(MediaType.APPLICATION_JSON_TYPE);
            return response.build();
        }
    }

    private ErrorResponse generateErrorResponse(final String errorMessage) {
        return new ErrorResponse(DateTimeUtils.getISOFormattedTimeStamp(), "500", errorMessage, "/");
    }
}
