import { ConfigPlugin, withAndroidManifest } from 'expo/config-plugins';

/** Fixes ML Kit meta-data conflict between expo-dev-launcher (barcode_ui) and idensic-mobile-sdk (face). */
export const withMlKitDependenciesMerge: ConfigPlugin = config => {
    return withAndroidManifest(config, config => {
        const manifest = config.modResults.manifest;
        manifest.$ = { ...manifest.$, 'xmlns:tools': 'http://schemas.android.com/tools' };

        const app = manifest.application?.[0];
        if (!app) {
            return config;
        }

        const name = 'com.google.mlkit.vision.DEPENDENCIES';
        const metaDataList = (app['meta-data'] ?? []) as { $?: Record<string, string> }[];
        const idx = metaDataList.findIndex(m => m?.$?.['android:name'] === name);

        const entry = {
            'android:name': name,
            'android:value': 'barcode_ui,face',
            'tools:replace': 'android:value',
        };
        if (idx >= 0) {
            metaDataList[idx].$ = { ...metaDataList[idx].$, ...entry };
        } else {
            app['meta-data'] = [...metaDataList, { $: entry }];
        }
        return config;
    });
};
