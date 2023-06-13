package com.achelos.task.restimpl.api;


import com.achelos.task.restimpl.models.ErrorResponse;
import com.achelos.task.restimpl.models.Status;

import com.achelos.task.restimpl.server.TesttoolRequestResource;
import com.achelos.task.utilities.DateTimeUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@Path("/status/{runId}")
@Tag(name = "Get Status", description = "Get the status of a test execution.")
public class StatusApi {

    @GET
    @Produces({ "application/json" })
    @Operation(summary = "Retrieve the status of a test suite execution.", description = "Retrieve the status of the test suite execution specified by the provided run identifier.", tags={ "Get Status" })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation=Status.class))),
        @ApiResponse(responseCode = "400", description = "RunId invalid.", content = @Content(schema = @Schema(implementation= ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "RunId not known.", content = @Content(schema = @Schema(implementation= ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation=ErrorResponse.class))) })
	public Response getStatus(@PathParam("runId") @Parameter(example = "a4b4d23234b23ef23a",
			description = "Identifier of a Testrun/Test Suite Execution.") String runId) {
        try {
            Response.ResponseBuilder response;
            // Parse runID into UUID.
            UUID runUuid;
            try {
                runUuid = UUID.fromString(runId);
            } catch (Exception e) {
                response = Response.serverError();
                response.status(Response.Status.BAD_REQUEST);
                var errorResponse = generateErrorResponse("400", "Bad Request: Illegal RunId: " + runId, runId);
                response.entity(errorResponse);
                return response.build();
            }
            var status = TesttoolRequestResource.getStatus(runUuid);
            if (status == TesttoolRequestResource.ExecutionStatus.UNKNOWN) {
                response = Response.serverError();
                response.status(Response.Status.NOT_FOUND);
                var errorResponse = generateErrorResponse("404", "Unknown RunId: " + runId, runId);
                response.entity(errorResponse);
            } else {
                Status.StatusEnum statusEnum;
                String downloadURI = "";
                switch (status) {
                    case RUNNING:
                        statusEnum = Status.StatusEnum.RUNNING;
                        break;
                    case FINISHED:
                        statusEnum = Status.StatusEnum.EXECUTED;
                        downloadURI = "/result/" + runUuid;
                        break;
                    case SCHEDULED:
                        statusEnum = Status.StatusEnum.SCHEDULED;
                        break;
                    case ABORTED:
                    default:
                        statusEnum = Status.StatusEnum.ABORTED;
                        break;
                }
                response = Response.ok().entity(new Status(statusEnum, downloadURI));
            }

            return response.build();
        } catch (Exception e) {
            var errorResponse = generateErrorResponse("500", "Internal Server Error: " + e.getMessage(), runId);
            var response = Response.serverError().entity(errorResponse);
            response.type(MediaType.APPLICATION_JSON_TYPE);
            return response.build();
        }
    }

    private ErrorResponse generateErrorResponse(final String statusCode, final String errorMessage, final String runId) {
        return new ErrorResponse(DateTimeUtils.getISOFormattedTimeStamp(), statusCode, errorMessage, "/status/" + runId);
    }
}
