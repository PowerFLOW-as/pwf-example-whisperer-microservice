package cz.pwf.whisperer.model.pwf;

import java.util.Map;
import lombok.Data;
import lombok.ToString;

/**
 * Třída zapouzdřující informace o uživateli (z Active Directory), který provádí daný požadavek (volání endpointu).
 * Dále poskutuje informaci o validnosti JWT tokenu.
 */
@Data
@ToString
public class TokenVerificationInfo {
    private boolean valid = false;
    private Map<String, Object> user;
}
