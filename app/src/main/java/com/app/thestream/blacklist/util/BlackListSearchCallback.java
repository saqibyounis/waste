package com.app.thestream.blacklist.util;


import com.app.thestream.blacklist.model.BlockedAppModel;

import java.util.ArrayList;

public interface BlackListSearchCallback {
    void onBlackListScanCompleted(boolean isBlackListDetected, ArrayList<BlockedAppModel> list);
}
