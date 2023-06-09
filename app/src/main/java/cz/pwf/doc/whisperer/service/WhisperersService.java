package cz.pwf.whisperer.service;

import cz.pwf.whisperer.pwf_ods_whisperers_api_client.handler.OdsSjzDeviceApi;
import cz.pwf.whisperer.pwf_ods_whisperers_api_client.handler.OdsWorkOrderTaskApi;
import cz.pwf.whisperer.pwf_ods_whisperers_api_client.model.ODSSjzDeviceResponse;
import cz.pwf.whisperer.pwf_ods_whisperers_api_client.model.ODSWorkOrderTaskResponse;
import cz.pwf.whisperer.config.Constants;
import cz.pwf.whisperer.model.SjzDevice;
import cz.pwf.whisperer.model.WorkOrderTask;
import cz.pwf.whisperer.model.mapper.SjzDeviceMapper;
import cz.pwf.whisperer.model.mapper.WorkOrderTaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servisní třída obsahující business logiku pro ziskávání dat z DWH.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public final class WhisperersService {

    private final static char LIKE_OPERATOR = '%';

    private final OdsWorkOrderTaskApi workOrderTaskApi;
    private final OdsSjzDeviceApi sjzDeviceApi;
    private final SjzDeviceMapper sjzDeviceMapper;
    private final WorkOrderTaskMapper workOrderTaskMapper;

    /**
     * Metoda pro získání dat o ÚPP (úkolech pracovních příkazů) z DWH.
     * @param workOrderTaskNumberLike část čísla ÚPP
     * @param limit limit počtu vrácených zařízení z DWH
     * @param workOrderStatus status pracovního příkazu
     * @param workOrderType typ pracovního příkazu
     * @param kpjm korporátní jméno uživatele, jehož jménem se vykonává požadavek
     * @return
     */
    public ResponseEntity<List<WorkOrderTask>> getWorkOrderTaskSuggestedData(String workOrderTaskNumberLike, Integer limit,
            Optional<List<String>> workOrderStatus, Optional<List<String>> workOrderType, String kpjm) {

        Instant start = Instant.now();

        workOrderTaskNumberLike = addLikeOperator(workOrderTaskNumberLike);

        ResponseEntity<List<ODSWorkOrderTaskResponse>> response =
                workOrderTaskApi.oDSWorkOrderTaskGetWorkOrderTasksWithHttpInfo(UUID.randomUUID().toString(),
                        String.valueOf(System.currentTimeMillis()), Constants.SOURCE_SYSTEM, encodeUrlParameter(kpjm),
                        null, null, null, null, null, null,
                        null, null, null,
                        workOrderTaskNumberLike, workOrderStatus.orElse(null), workOrderType.orElse(null), limit);
        logEndpointCallElapsedTime(start, "oDSWorkOrderTaskGetWorkOrder");

        List<WorkOrderTask> convertedResults = new ArrayList<>();
        if (response.hasBody()) {
            convertedResults = Objects.requireNonNull(response.getBody()).stream()
                    .map(workOrderTaskMapper::toWorkOrderTask)
                    .collect(Collectors.toList());
        }

        return new ResponseEntity<>(convertedResults, response.getStatusCode());
    }

    /**
     * Metoda pro získání dat o SJZ zařízeních z DWH, která nejsou typu komponenta.
     * @param sjzDeviceNumberLike část šísla SJZ zařízení
     * @param facility název elektrárny
     * @param unit označení bloku elektrárny
     * @param limit limit počtu vrácených zařízení z DWH
     * @param kpjm korporátní jméno uživatele, jehož jménem se vykonává požadavek
     * @return Vrací seznam SJZ zařízení, která nejsou typu komponenta.
     */
    public ResponseEntity<List<SjzDevice>> getWorkSjzDeviceSuggestedData(String sjzDeviceNumberLike, String facility, String unit, Integer limit, String kpjm) {
        return getWorkSjzDeviceSuggestedDataInternal(null, sjzDeviceNumberLike, null, facility, unit, limit, kpjm, false);
    }

    /**
     * Metoda pro získání dat o SJZ zařízeních typu komponenta z DWH.
     * @param sjzDeviceNumber celé šíslo SJZ zařízení
     * @param componentNumberLike část čísla komponenty
     * @param facility název elektrárny
     * @param unit označení bloku elektrárny
     * @param limit limit počtu vrácených zařízení z DWH
     * @param kpjm korporátní jméno uživatele, jehož jménem se vykonává požadavek
     * @return Vrací seznam SJZ zařízení typu komponenta.
     */
    public ResponseEntity<List<SjzDevice>> getWorkSjzDeviceComponentSuggestedData(String sjzDeviceNumber, String componentNumberLike, String facility, String unit, Integer limit, String kpjm) {
        return getWorkSjzDeviceSuggestedDataInternal(sjzDeviceNumber, null, componentNumberLike, facility, unit, limit, kpjm, true);
    }

    /**
     * Interně používaná metoda pro získání dat o SJZ zařízeních z DWH.
     * @param sjzDeviceNumber celé šíslo SJZ zařízení
     * @param sjzDeviceNumberLike část šísla SJZ zařízení
     * @param componentNumberLike část čísla komponenty
     * @param facility název elektrárny
     * @param unit označení bloku elektrárny
     * @param limit limit počtu vrácených zařízení z DWH
     * @param kpjm korporátní jméno uživatele, jehož jménem se vykonává požadavek
     * @param component parametr nabývá hodnoty true/false a určuje, zda se má vyhledat pouze zařízení typu komponenta
     * @return Vrací seznam SJZ zařízení.
     */
    private ResponseEntity<List<SjzDevice>> getWorkSjzDeviceSuggestedDataInternal(String sjzDeviceNumber, String sjzDeviceNumberLike,
                                                                                  String componentNumberLike, String facility, String unit,
                                                                                  Integer limit, String kpjm, boolean component) {

        sjzDeviceNumberLike = addLikeOperator(sjzDeviceNumberLike);
        componentNumberLike = addLikeOperator(componentNumberLike);

        Instant start = Instant.now();

        ResponseEntity<List<ODSSjzDeviceResponse>> response =
                sjzDeviceApi.oDSSjzDeviceGetSjzDevicesWithHttpInfo(encodeUrlParameter(kpjm), UUID.randomUUID().toString(),
                        String.valueOf(System.currentTimeMillis()), Constants.SOURCE_SYSTEM, null, null,
                        null, null, sjzDeviceNumber, sjzDeviceNumberLike, null,
                        componentNumberLike, facility, unit, limit, component);
        logEndpointCallElapsedTime(start, "oDSSjzDeviceGetSjzDevices");

        List<SjzDevice> convertedResults = new ArrayList<>();
        if (response.hasBody()) {
            convertedResults = Objects.requireNonNull(response.getBody()).stream()
                    .map(sjzDeviceMapper::toSjzDevice)
                    .collect(Collectors.toList());
        }

        return new ResponseEntity<>(convertedResults, response.getStatusCode());
    }

    /**
     * Metoda přidává k hledané hodnotě LIKE operátor pro doplnění libovolného počtu znaků zprava.
     * @param searchingValue část hledané hodnoty, doplňované libovolným počtem znaků zprava
     * @return Vrací hledanou hodnotu doplněnou zprava o operátor LIKE pro databázové vyhledávání.
     */
    private String addLikeOperator(String searchingValue) {
        if (StringUtils.hasText(searchingValue) && LIKE_OPERATOR != searchingValue.charAt(searchingValue.length() - 1)) {
            searchingValue += LIKE_OPERATOR;
        }

        return searchingValue;
    }

    /**
     * Metoda loguje čas strávený zpracováním HTTP požadavku.
     * @param start začátek zpracování požadavku
     * @param endpointName název endpointu, u kterého se sleduje doba zpracování požadavku
     */
    private void logEndpointCallElapsedTime(Instant start, String endpointName) {
        if (log.isDebugEnabled()) {
            Instant finish = Instant.now();
            log.debug("Call {} endpoint - elapsed time: {} ms", endpointName, Duration.between(start, finish).toMillis());
        }
    }

    /**
     * Metoda kóduje vstupní řetězec pomocí třídy {@link URLEncoder} tak, aby obsahoval kompatibilní znaky pro adresu URL.
     * @param parameter neošetřený vstupní řetězec, který má být součástí URL
     * @return Vrací řetězec ošetřený před speciálními znaky (nekompatibilními s URL adresuou).
     */
    private String encodeUrlParameter(String parameter) {
        return URLEncoder.encode(parameter, StandardCharsets.UTF_8);
    }
}
