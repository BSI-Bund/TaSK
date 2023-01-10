package com.achelos.task.restimpl.api;

import com.achelos.task.restimpl.models.ErrorResponse;
import com.achelos.task.restimpl.models.RunId;
import com.achelos.task.restimpl.server.TaskRequestEntry;
import com.achelos.task.restimpl.server.TesttoolRequestResource;
import com.achelos.task.utilities.DateTimeUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Path("/execute")
@Tag(name = "Start Testrun Execution", description = "Execution of the TaSK Framework.")
public class ExecuteApi {

    @POST
    @Path("/mics")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces({"application/json"})
    @Operation(summary = "Execute a Testrun of the TaSK Framework using a MICS file.",
            description = "Execute the TaSK Framework with the provided MICS file.",
            tags = {"Start Execution"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Accepted", content = @Content(schema = @Schema(implementation = RunId.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response executeFromMICS(@FormDataParam("micsFile") File micsFile,
                                    @FormDataParam("serverCertificateChain") List<FormDataBodyPart> serverCertificateChain,
                                    @FormDataParam("ignoreMicsVerificationResult") Boolean ignoreMicsVerificationResult,
                                    @FormDataParam("clientAuthCertificateChain") List<FormDataBodyPart> clientAuthCertificateChain,
                                    @FormDataParam("clientAuthKey") File clientAuthKey) {
        try {
            Response.ResponseBuilder response;
            if (micsFile == null || !micsFile.exists()) {
                response = Response.serverError();
                response.status(Response.Status.fromStatusCode(400));
                response.type(MediaType.APPLICATION_JSON_TYPE);
                var errorResponse = generateErrorResponse("400", "Bad Request: Received MICS file is null.", "mics");
                response.entity(errorResponse);
            } else {
                var ignoreMicsFlag = ignoreMicsVerificationResult != null && ignoreMicsVerificationResult;
                var requestUuid = UUID.randomUUID();
                var serverCertChainList = new ArrayList<InputStream>();
                if (serverCertificateChain != null) {
                    for (var bodyPart : serverCertificateChain) {
                        serverCertChainList.add(bodyPart.getValueAs(InputStream.class));
                    }
                }
                var clientAuthCertificateChainList = new ArrayList<InputStream>();
                if (clientAuthCertificateChain != null) {
                    for (var bodyPart : clientAuthCertificateChain) {
                        clientAuthCertificateChainList.add(bodyPart.getValueAs(InputStream.class));
                    }
                }
                TaskRequestEntry taskRequestEntry;
                try {
                    taskRequestEntry = new TaskRequestEntry(requestUuid, micsFile, serverCertChainList, ignoreMicsFlag, clientAuthCertificateChainList, clientAuthKey);
                } catch (Exception e) {
                    response = Response.serverError();
                    response.status(Response.Status.fromStatusCode(400));
                    response.type(MediaType.APPLICATION_JSON_TYPE);
                    var errorResponse = generateErrorResponse("400", "Bad Request: Received MICS could not be parsed: " + e.getMessage(), "mics");
                    response.entity(errorResponse);
                    return response.build();
                }
                var queuingResult = TesttoolRequestResource.queueExecution(taskRequestEntry);
                if (queuingResult) {
                    response = Response.accepted(new RunId(requestUuid.toString()));
                } else {
                    response = Response.serverError();
                    response.status(Response.Status.fromStatusCode(503));
                    response.type(MediaType.APPLICATION_JSON_TYPE);
                    var errorResponse = generateErrorResponse("503", "Service currently unavailable: Execution Queue of TaSK Server is full. Try again later.", "mics");
                    response.entity(errorResponse);
                }
            }
            return response.build();
        } catch (Exception e) {
            var errorResponse = generateErrorResponse("500", "Internal Server Error: " + e.getMessage(), "mics");
            var response = Response.serverError().entity(errorResponse);
            response.type(MediaType.APPLICATION_JSON_TYPE);
            return response.build();
        }
    }

    @POST
    @Path("/trp")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces({"application/json"})
    @Operation(summary = "Execute a Testrun of the TaSK Framework from a given Test Runplan.",
            description = "Execute a Testrun of the TaSK Framework with the provided Test Runplan file.",
            tags = {"Start Execution"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Accepted", content = @Content(schema = @Schema(implementation = RunId.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response executeFromRunplan(@FormDataParam("testRunPlanFile") File testRunPlanFile, @FormDataParam("clientAuthCertificateChain") List<File> clientAuthCertificateChain, @FormDataParam("clientAuthKey") File clientAuthKey) {

        try {
            Response.ResponseBuilder response;
            if (testRunPlanFile == null || !testRunPlanFile.exists()) {
                response = Response.serverError();
                response.status(Response.Status.fromStatusCode(400));
                response.type(MediaType.APPLICATION_JSON_TYPE);
                var errorResponse = generateErrorResponse("400", "Bad Request: Received TestRunPlan file is null.", "trp");
                response.entity(errorResponse);
            } else {
                var requestUuid = UUID.randomUUID();
                TaskRequestEntry taskRequestEntry;
                try {
                    taskRequestEntry = new TaskRequestEntry(requestUuid, testRunPlanFile, clientAuthCertificateChain, clientAuthKey);
                } catch (Exception e) {
                    response = Response.serverError();
                    response.status(Response.Status.fromStatusCode(400));
                    response.type(MediaType.APPLICATION_JSON_TYPE);
                    var errorResponse = generateErrorResponse("400", "Bad Request: Received TestRunplan could not be parsed: " + e.getMessage(), "trp");
                    response.entity(errorResponse);
                    return response.build();
                }
                var queuingResult = TesttoolRequestResource.queueExecution(taskRequestEntry);
                if (queuingResult) {
                    response = Response.accepted(new RunId(requestUuid.toString()));
                } else {
                    response = Response.serverError();
                    response.status(Response.Status.fromStatusCode(503));
                    response.type(MediaType.APPLICATION_JSON_TYPE);
                    var errorResponse = generateErrorResponse("503", "Service currently unavailable: Execution Queue of TaSK Server is full. Try again later.", "mics");
                    response.entity(errorResponse);
                }
            }
            return response.build();
        } catch (Exception e) {
            var errorResponse = generateErrorResponse("500", "Internal Server Error: " + e.getMessage(), "trp");
            var response = Response.serverError().entity(errorResponse);
            response.type(MediaType.APPLICATION_JSON_TYPE);
            return response.build();
        }
    }

    private ErrorResponse generateErrorResponse(final String statusCode, final String errorMessage, final String subPath) {
        return new ErrorResponse(DateTimeUtils.getISOFormattedTimeStamp(), statusCode, errorMessage, "/execute/" + subPath);
    }
}
