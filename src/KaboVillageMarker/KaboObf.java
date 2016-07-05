package KaboVillageMarker;

import com.mumfrey.liteloader.core.runtime.Obf;

/**
 * @author guntherdw
 */
public class KaboObf extends Obf {
    /**
     * @param seargeName
     * @param obfName
     */
    protected KaboObf(String seargeName, String obfName) {
        super(seargeName, obfName, seargeName);
    }

    /**
     * @param seargeName
     * @param obfName
     * @param mcpName
     */
    protected KaboObf(String seargeName, String obfName, String mcpName) {
        super(seargeName, obfName, mcpName);
    }

    // public static KaboObf
}
