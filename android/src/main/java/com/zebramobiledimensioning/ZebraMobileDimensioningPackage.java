package com.zebramobiledimensioning;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.facebook.react.module.model.ReactModuleInfo;
import com.facebook.react.module.model.ReactModuleInfoProvider;

public class ZebraMobileDimensioningPackage implements ReactPackage {

  @Override
  public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
    List<NativeModule> modules = new ArrayList<>();
    modules.add(new ZebraMobileDimensioningModule(reactContext)); // Ensure ZebraMobileDimensioningModule exists and is properly referenced
    return modules;
  }

  @Override
  public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
    return Collections.emptyList(); // No view managers in this package
  }

  public ReactModuleInfoProvider getReactModuleInfoProvider() {
    return new ReactModuleInfoProvider() {
      @Override
      public Map<String, ReactModuleInfo> getReactModuleInfos() {
        Map<String, ReactModuleInfo> moduleInfos = new HashMap<>();
        moduleInfos.put(ZebraMobileDimensioningModule.NAME, new ReactModuleInfo(
          ZebraMobileDimensioningModule.NAME,
          ZebraMobileDimensioningModule.NAME,
          false,  // canOverrideExistingModule
          false,  // needsEagerInit
          false,  // isCxxModule
          true    // isTurboModule
        ));
        return moduleInfos;
      }
    };
  }
}
