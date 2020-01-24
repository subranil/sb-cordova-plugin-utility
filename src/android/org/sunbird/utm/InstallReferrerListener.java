package org.sunbird.utm;

import java.util.Map;

public interface InstallReferrerListener {
    void onHandlerReferrer(Map<String, String> properties);
}
