package cz.pwf.whisperer.controller;

import cz.pwf.whisperer.config.Constants;
import cz.pwf.whisperer.model.SjzDevice;
import cz.pwf.whisperer.model.WorkOrderTask;
import cz.pwf.whisperer.service.WhisperersService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller třída definující endpointy vystavované směrem k systému PowerFLOW pro potřeby našeptávačů.
 */
@RestController
@Tag(name = "whisperers", description = "Whisperers API - Endpointy pro našeptávače")
@RequiredArgsConstructor
public class WhisperersController implements WhisperersApi {

    private final WhisperersService whisperersService;
    private final HttpServletRequest request;

    @Override
    public ResponseEntity<List<WorkOrderTask>> getWorkOrderTaskSuggestedData(@NotNull @Valid String workOrderTaskNumber,
            @NotNull @Valid Integer limit, @Valid Optional<List<String>> workOrderStatus, @Valid Optional<List<String>> workOrderType) {
        return whisperersService.getWorkOrderTaskSuggestedData(workOrderTaskNumber, limit, workOrderStatus, workOrderType,
                request.getHeader(Constants.KPJM_HEADER_NAME));
    }

    @Override
    public ResponseEntity<List<SjzDevice>> getWorkSjzDeviceSuggestedData(@NotNull @Valid String sjzDeviceNumber, @NotNull @Valid Integer limit,
            @Valid Optional<String> facility, @Valid Optional<String> unit) {
        return whisperersService.getWorkSjzDeviceSuggestedData(sjzDeviceNumber, facility.orElse(null),
                unit.orElse(null), limit, request.getHeader(Constants.KPJM_HEADER_NAME));
    }

    @Override
    public ResponseEntity<List<SjzDevice>> getWorkSjzDeviceComponentSuggestedData(@NotNull @Valid String sjzDeviceNumber,
            @NotNull @Valid String componentNumber, @NotNull @Valid Integer limit, @Valid Optional<String> facility, @Valid Optional<String> unit) {
        return whisperersService.getWorkSjzDeviceComponentSuggestedData(sjzDeviceNumber, componentNumber,
                facility.orElse(null), unit.orElse(null), limit, request.getHeader(Constants.KPJM_HEADER_NAME));
    }

}
