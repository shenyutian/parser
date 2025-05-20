package org.syt.parser.apk.parser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.syt.parser.entry.*;
import org.syt.parser.apk.bean.IconPath;
import org.syt.parser.apk.struct.ResourceValue;
import org.syt.parser.apk.struct.resource.Densities;
import org.syt.parser.apk.struct.resource.ResourceEntry;
import org.syt.parser.apk.struct.resource.ResourceTable;
import org.syt.parser.apk.struct.resource.Type;
import org.syt.parser.apk.struct.xml.*;
import org.syt.parser.log.Log;

import java.util.*;

/**
 * trans binary xml to apk meta info
 *
 * @author Liu Dong dongliu@live.cn
 */
public class ApkMetaTranslator implements XmlStreamer {
    private String[] tagStack = new String[100];
    private int depth = 0;
    private ApkMeta apkMeta = new ApkMeta();
    private List<IconPath> iconPaths = Collections.emptyList();

    private ResourceTable resourceTable;
    @Nullable
    private Locale locale;

    private IntentFilter intentFilter;
    private AndroidComponent component;

    public ApkMetaTranslator(ResourceTable resourceTable, @Nullable Locale locale) {
        this.resourceTable = Objects.requireNonNull(resourceTable);
        this.locale = locale;
    }

    @Override
    public void onStartTag(XmlNodeStartTag xmlNodeStartTag) {
        Attributes attributes = xmlNodeStartTag.getAttributes();
        switch (xmlNodeStartTag.getName()) {
            case "application":
                boolean debuggable = attributes.getBoolean("debuggable", false);
                apkMeta.getApplication().setDebuggable(debuggable);
                String label = attributes.getString("label");
                if (label != null) {
                    apkMeta.setLabel(label);
                    apkMeta.getApplication().setLabel(label);
                }
                Attribute iconAttr = attributes.get("icon");
                if (iconAttr != null) {
                    ResourceValue resourceValue = iconAttr.getTypedValue();
                    if (resourceValue instanceof ResourceValue.ReferenceResourceValue) {
                        long resourceId = ((ResourceValue.ReferenceResourceValue) resourceValue).getReferenceResourceId();
                        List<ResourceTable.Resource> resources = this.resourceTable.getResourcesById(resourceId);
                        if (!resources.isEmpty()) {
                            List<IconPath> icons = new ArrayList<>();
                            boolean hasDefault = false;
                            for (ResourceTable.Resource resource : resources) {
                                Type type = resource.getType();
                                ResourceEntry resourceEntry = resource.getResourceEntry();
                                String path = resourceEntry.toStringValue(resourceTable, locale);
                                if (type.getDensity() == Densities.DEFAULT) {
                                    hasDefault = true;
                                    apkMeta.setIcon(path);
                                }
                                IconPath iconPath = new IconPath(path, type.getDensity());
                                icons.add(iconPath);
                            }
                            if (!hasDefault) {
                                apkMeta.setIcon(icons.get(0).getPath());
                            }
                            this.iconPaths = icons;
                        }
                    } else {
                        String value = iconAttr.getValue();
                        if (value != null) {
                            apkMeta.setIcon(value);
                            IconPath iconPath = new IconPath(value, Densities.DEFAULT);
                            this.iconPaths = Collections.singletonList(iconPath);
                        }
                    }
                }
                break;
            case "manifest":
                apkMeta.setPackageName(attributes.getString("package"));
                apkMeta.setVersionName(attributes.getString("versionName"));
                apkMeta.setRevisionCode(attributes.getLong("revisionCode"));
                apkMeta.setSharedUserId(attributes.getString("sharedUserId"));
                apkMeta.setSharedUserLabel(attributes.getString("sharedUserLabel"));
                apkMeta.setSplit(attributes.getString("split"));
                apkMeta.setConfigForSplit(attributes.getString("configForSplit"));
                apkMeta.setFeatureSplit(attributes.getBoolean("isFeatureSplit", false));
                apkMeta.setSplitRequired(attributes.getBoolean("isSplitRequired", false));
                apkMeta.setIsolatedSplits(attributes.getBoolean("isolatedSplits", false));

                Long majorVersionCode = attributes.getLong("versionCodeMajor");
                Long versionCode = attributes.getLong("versionCode");
                if (majorVersionCode != null) {
                    if (versionCode == null) {
                        versionCode = 0L;
                    }
                    versionCode = (majorVersionCode << 32) | (versionCode & 0xFFFFFFFFL);
                }
                apkMeta.setVersionCode(versionCode);

                String installLocation = attributes.getString("installLocation");
                if (installLocation != null) {
                    apkMeta.setInstallLocation(installLocation);
                }
                apkMeta.setCompileSdkVersion(attributes.getString("compileSdkVersion"));
                apkMeta.setCompileSdkVersionCodename(attributes.getString("compileSdkVersionCodename"));
                apkMeta.setPlatformBuildVersionCode(attributes.getString("platformBuildVersionCode"));
                apkMeta.setPlatformBuildVersionName(attributes.getString("platformBuildVersionName"));
                break;
            case "uses-sdk":
                String minSdkVersion = attributes.getString("minSdkVersion");
                if (minSdkVersion != null) {
                    apkMeta.setMinSdkVersion(minSdkVersion);
                }
                String targetSdkVersion = attributes.getString("targetSdkVersion");
                if (targetSdkVersion != null) {
                    apkMeta.setTargetSdkVersion(targetSdkVersion);
                }
                String maxSdkVersion = attributes.getString("maxSdkVersion");
                if (maxSdkVersion != null) {
                    apkMeta.setMaxSdkVersion(maxSdkVersion);
                }
                break;
            case "supports-screens":
                apkMeta.setAnyDensity(attributes.getBoolean("anyDensity", false));
                apkMeta.setSmallScreens(attributes.getBoolean("smallScreens", false));
                apkMeta.setNormalScreens(attributes.getBoolean("normalScreens", false));
                apkMeta.setLargeScreens(attributes.getBoolean("largeScreens", false));
                break;
            case "uses-feature":
                String name = attributes.getString("name");
                boolean required = attributes.getBoolean("required", false);
                if (name != null) {
                    UseFeature useFeature = new UseFeature(name, required);
                    apkMeta.addUsesFeature(useFeature);
                } else {
                    Integer gl = attributes.getInt("glEsVersion");
                    if (gl != null) {
                        int v = gl;
                        GlEsVersion glEsVersion = new GlEsVersion(v >> 16, v & 0xffff, required);
                        apkMeta.setGlEsVersion(glEsVersion);
                    }
                }
                break;
            case "uses-permission":
                apkMeta.addUsesPermission(attributes.getString("name"));
                break;
            case "permission":
                Permission permission = new Permission(
                        attributes.getString("name"),
                        attributes.getString("label"),
                        attributes.getString("icon"),
                        attributes.getString("description"),
                        attributes.getString("group"),
                        attributes.getString("android:protectionLevel"));
                apkMeta.addPermissions(permission);
                break;
            // below for server / activity / receiver
            case "service":
                Service service = new Service();
                fillComponent(attributes, service);
                component = service;
                break;
            case "activity-alias":
                ActivityAlias activityAlias = new ActivityAlias();
                fillComponent(attributes, activityAlias);
                component = activityAlias;
                apkMeta.addActivityAlias(activityAlias);
                break;
            case "activity":
                Activity activity = new Activity();
                fillComponent(attributes, activity);
                component = activity;
                apkMeta.addActivity(activity);
                break;
            case "receiver":
                Receiver receiver = new Receiver();
                fillComponent(attributes, receiver);
                component = receiver;
                apkMeta.addReceiver(receiver);
                break;
            // below is for intent filter
            case "intent-filter":
                if (matchLastTag("activity") || matchLastTag("receiver") || matchLastTag("service")
                        || matchLastTag("activity-alias")) {
                    intentFilter = new IntentFilter();
                }
                break;
            case "action":
                if (matchLastTag("intent-filter")) {
                    if (intentFilter != null) {
                        intentFilter.addAction(attributes.getString("name"));
                    } else {
//                        System.out.println("intentFilter is null " + attributes.getString("name"));
                    }
                }
                break;
            case "category":
                if (matchLastTag("intent-filter")) {
                    if (intentFilter != null) {
                        intentFilter.addCategory(attributes.getString("name"));
                    } else {
//                        System.out.println("intentFilter is null " + attributes.getString("name"));
                    }
                }
                break;
            case "data":
                if (matchLastTag("intent-filter")) {
                    String scheme = attributes.getString("scheme");
                    String host = attributes.getString("host");
                    String pathPrefix = attributes.getString("pathPrefix");
                    String mimeType = attributes.getString("mimeType");
                    String type = attributes.getString("type");
                    IntentFilter.IntentData data = new IntentFilter.IntentData();
                    data.setScheme(scheme);
                    data.setMimeType(mimeType);
                    data.setHost(host);
                    data.setPathPrefix(pathPrefix);
                    data.setType(type);
                    intentFilter.addData(data);
                }
                break;

        }
        tagStack[depth++] = xmlNodeStartTag.getName();
    }

    @Override
    public void onEndTag(XmlNodeEndTag xmlNodeEndTag) {
        depth--;
        switch (xmlNodeEndTag.getName()) {
            // below for server / activity / receiver
            case "service":
                apkMeta.addService((Service) component);
                component = null;
                break;
            case "activity":
                apkMeta.addActivity((Activity) component);
                component = null;
                break;
            case "receiver":
                apkMeta.addReceiver((Receiver) component);
                component = null;
                break;
            case "intent-filter":
                if (matchLastTag("activity") || matchLastTag("receiver") || matchLastTag("service")
                        || matchLastTag("activity-alias")) {
                    apkMeta.addIntentFilter(intentFilter);
                    component.addIntentFilter(intentFilter);
                    intentFilter = null;
                }
                break;
        }
    }

    private void fillComponent(Attributes attributes, org.syt.parser.entry.AndroidComponent component) {
        component.setName(attributes.getString("name"));
        component.setExported(attributes.getBoolean("exported", false));
        component.setProcess(attributes.getString("process"));
    }


    @Override
    public void onCData(XmlCData xmlCData) {

    }

    @Override
    public void onNamespaceStart(XmlNamespaceStartTag tag) {

    }

    @Override
    public void onNamespaceEnd(XmlNamespaceEndTag tag) {

    }

    @NotNull
    public ApkMeta getApkMeta() {
        return apkMeta;
    }

    @NotNull
    public List<IconPath> getIconPaths() {
        return iconPaths;
    }

    private boolean matchTagPath(String... tags) {
        // the root should always be "manifest"
        if (depth != tags.length + 1) {
            return false;
        }
        for (int i = 1; i < depth; i++) {
            if (!tagStack[i].equals(tags[i - 1])) {
                return false;
            }
        }
        return true;
    }

    private boolean matchLastTag(String tag) {
        // the root should always be "manifest"
        return tagStack[depth - 1].endsWith(tag);
    }
}
