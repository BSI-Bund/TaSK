package com.achelos.task.restimpl.api;

import com.achelos.task.restimpl.models.ErrorResponse;
import com.achelos.task.restimpl.server.TesttoolRequestResource;
import com.achelos.task.utilities.DateTimeUtils;
import com.achelos.task.utilities.FileUtils;
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
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Path("/result/{runId}")
@Tag(name = "Get Results", description = "Get the result of a test execution.")
public class ResultApi {

    @GET
    @Path("/pdf")
    @Produces({ "application/pdf", MediaType.APPLICATION_JSON})
    @Operation(summary = "Retrieve the PDF report of a test suite execution.", description = "Retrieve PDF report of the test suite execution specified by the run identifier.", tags={ "Get Results" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation=Object.class))),
            @ApiResponse(responseCode = "400", description = "RunId invalid.", content = @Content(schema = @Schema(implementation= ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "TestrunId not known.", content = @Content(schema = @Schema(implementation=ErrorResponse.class))),
            @ApiResponse(responseCode = "424", description = "Test execution not finished.", content = @Content(schema = @Schema(implementation=ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation=ErrorResponse.class))) })
    public Response getReportPdf(@PathParam("runId") @Parameter(description = "Identifier of a Testrun/Test Suite Execution.") String runId) {
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
            // Retrieve status for the UUID.
            var status = TesttoolRequestResource.getStatus(runUuid);


            if (status == TesttoolRequestResource.ExecutionStatus.FINISHED) {
                var resultPath = TesttoolRequestResource.getResultPathString(runUuid);
                if (!resultPath.isBlank()) {
                    var report_pdf_file = Paths.get(resultPath, "/Report.pdf").toFile();
                    if (report_pdf_file.exists() && !report_pdf_file.isDirectory()) {
                        var report_pdf = Files.readAllBytes(report_pdf_file.toPath());
                        response = Response.ok(report_pdf);
                        response.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + "TestReport_" + runId + ".pdf");
                    } else {
                        response = Response.serverError();
                        response.type(MediaType.APPLICATION_JSON_TYPE);
                        var errorResponse = generateErrorResponse("500", "PDF Report unavailable.", runId + "/pdf");
                        response.entity(errorResponse);
                    }
                } else {
                    response = Response.serverError();
                    response.status(Response.Status.NOT_FOUND);
                    response.type(MediaType.APPLICATION_JSON_TYPE);
                    var errorResponse = generateErrorResponse("404", "Unknown RunId: " + runId, runId + "/pdf");
                    response.entity(errorResponse);
                }
            } else {
                if (status == TesttoolRequestResource.ExecutionStatus.SCHEDULED) {
                    response = Response.serverError();
                    response.status(Response.Status.fromStatusCode(424));
                    response.type(MediaType.APPLICATION_JSON_TYPE);
                    var errorResponse = generateErrorResponse("424", "Test execution not finished for RunID: " + runId, runId + "/pdf");
                    response.entity(errorResponse);
                } else {
                    response = Response.serverError();
                    response.status(Response.Status.NOT_FOUND);
                    response.type(MediaType.APPLICATION_JSON_TYPE);
                    var errorResponse = generateErrorResponse("404", "Unknown RunId: " + runId, runId + "/pdf");
                    response.entity(errorResponse);
                }
            }

            return response.build();
        } catch (Exception e) {
            var errorResponse = generateErrorResponse("500", "Internal Server Error: " + e.getMessage(), runId + "/pdf");
            var response = Response.serverError().entity(errorResponse);
            response.type(MediaType.APPLICATION_JSON_TYPE);
            return response.build();
        }
    }

    @GET
    @Path("/xml")
    @Produces({ "application/xml", MediaType.APPLICATION_JSON})
    @Operation(summary = "Retrieve the XML report of a test suite execution.", description = "Retrieve XML report of the test suite execution specified by the run identifier.", tags={ "Get Results" })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation=Object.class))),
        @ApiResponse(responseCode = "400", description = "RunId invalid.", content = @Content(schema = @Schema(implementation= ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "TestrunId not known.", content = @Content(schema = @Schema(implementation=ErrorResponse.class))),
        @ApiResponse(responseCode = "424", description = "Test execution not finished.", content = @Content(schema = @Schema(implementation=ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation=ErrorResponse.class))) })
    public Response getReportXml(@PathParam("runId") @Parameter(description = "Identifier of a Testrun/Test Suite Execution.") String runId) {
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
            // Retrieve status for the UUID.
            var status = TesttoolRequestResource.getStatus(runUuid);


            if (status == TesttoolRequestResource.ExecutionStatus.FINISHED) {
                var resultPath = TesttoolRequestResource.getResultPathString(runUuid);
                if (!resultPath.isBlank()) {
                    var report_xml_file = Paths.get(resultPath, "/Report.xml").toFile();
                    if (report_xml_file.exists() && !report_xml_file.isDirectory()) {
                        var report_xml = Files.readString(Paths.get(resultPath, "Report.xml"), StandardCharsets.UTF_8);
                        response = Response.ok(report_xml);
                        response.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + "TestReport_" + runId + ".xml");
                    } else {
                        response = Response.serverError();
                        response.type(MediaType.APPLICATION_JSON_TYPE);
                        var errorResponse = generateErrorResponse("500", "PDF Report unavailable. " + runId, runId + "/xml");
                        response.entity(errorResponse);
                    }
                } else {
                    response = Response.serverError();
                    response.status(Response.Status.NOT_FOUND);
                    response.type(MediaType.APPLICATION_JSON_TYPE);
                    var errorResponse = generateErrorResponse("404", "Unknown RunId: " + runId, runId + "/xml");
                    response.entity(errorResponse);
                }
            } else {
                if (status == TesttoolRequestResource.ExecutionStatus.SCHEDULED) {
                    response = Response.serverError();
                    response.status(Response.Status.fromStatusCode(424));
                    response.type(MediaType.APPLICATION_JSON_TYPE);
                    var errorResponse = generateErrorResponse("424", "Test execution not finished for RunID: " + runId, runId + "/xml");
                    response.entity(errorResponse);
                } else {
                    response = Response.serverError();
                    response.status(Response.Status.NOT_FOUND);
                    response.type(MediaType.APPLICATION_JSON_TYPE);
                    var errorResponse = generateErrorResponse("404", "Unknown RunId: " + runId, runId + "/xml");
                    response.entity(errorResponse);
                }
            }
            return response.build();
        } catch (Exception e) {
            var errorResponse = generateErrorResponse("500", "Internal Server Error: " + e.getMessage(), runId + "/xml");
            var response = Response.serverError().entity(errorResponse);
            response.type(MediaType.APPLICATION_JSON_TYPE);
            return response.build();
        }
    }

    @GET
    @Produces({ "application/octet-string", MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve the results of a test run.", description = "Retrieve the results of the test suite execution specified by the provided run identifier.", tags={ "Get Results" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation=Object.class))),
            @ApiResponse(responseCode = "400", description = "RunId invalid.", content = @Content(schema = @Schema(implementation= ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "TestrunId not known.", content = @Content(schema = @Schema(implementation=ErrorResponse.class))),
            @ApiResponse(responseCode = "424", description = "Test execution not finished.", content = @Content(schema = @Schema(implementation=ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation= ErrorResponse.class))) })
    public Response getResult(@PathParam("runId") @Parameter(description =  "Identifier of a Testrun/Test Suite Execution.") String runId) {
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
            // Retrieve status for the UUID.
            var status = TesttoolRequestResource.getStatus(runUuid);

            if (status == TesttoolRequestResource.ExecutionStatus.FINISHED) {
                var resultPath = TesttoolRequestResource.getResultPathString(runUuid);
                if (!resultPath.isBlank()) {
                    var zipFile = getZippedReportFolder(resultPath);
                    if (!zipFile.isBlank()) {
                        var report_folder_zip = Files.readAllBytes(Paths.get(zipFile));
                        response = Response.ok(report_folder_zip);
                        response.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + "TestReport_" + runId + ".zip");
                    } else {
                        response = Response.serverError();
                        response.type(MediaType.APPLICATION_JSON_TYPE);
                        var errorResponse = generateErrorResponse("500", "Error creating report archive for run Id " + runId, runId);
                        response.entity(errorResponse);
                    }
                } else {
                    response = Response.serverError();
                    response.status(Response.Status.NOT_FOUND);
                    response.type(MediaType.APPLICATION_JSON_TYPE);
                    var errorResponse = generateErrorResponse("404", "Unknown RunId: " + runId, runId);
                    response.entity(errorResponse);
                }
            } else {
                if (status == TesttoolRequestResource.ExecutionStatus.SCHEDULED) {
                    response = Response.serverError();
                    response.status(Response.Status.fromStatusCode(424));
                    response.type(MediaType.APPLICATION_JSON_TYPE);
                    var errorResponse = generateErrorResponse("424", "Test execution not finished for RunID: " + runId, runId);
                    response.entity(errorResponse);
                } else {
                    response = Response.serverError();
                    response.status(Response.Status.NOT_FOUND);
                    response.type(MediaType.APPLICATION_JSON_TYPE);
                    var errorResponse = generateErrorResponse("404", "Unknown RunId: " + runId, runId);
                    response.entity(errorResponse);
                }
            }
            return response.build();
        } catch (Exception e) {
            var errorResponse = generateErrorResponse("500", "Internal Server Error: " + e.getMessage(), runId);
            var response = Response.serverError().entity(errorResponse);
            response.type(MediaType.APPLICATION_JSON_TYPE);
            return response.build();
        }
    }

    private String getZippedReportFolder(String resultPath) {
        if (resultPath.isBlank()) {
            return "";
        }
        var resultPathFile = new File(resultPath);
        if (!resultPathFile.exists() || !resultPathFile.isDirectory()) {
            return "";
        }
        var resultZipFile = resultPathFile.toPath().resolve("report.zip").toFile();
        if (resultZipFile.exists()) {
            return resultZipFile.getAbsolutePath();
        } else {
            try {
                FileUtils.zipFolder(resultPathFile, resultZipFile.getAbsoluteFile());
                return resultZipFile.getAbsolutePath();
            } catch (Exception e) {
                return "";
            }
        }
    }

    private ErrorResponse generateErrorResponse(final String statusCode, final String errorMessage, final String subPath) {
        return new ErrorResponse(DateTimeUtils.getISOFormattedTimeStamp(), statusCode, errorMessage, "/result/" + subPath);
    }
}
