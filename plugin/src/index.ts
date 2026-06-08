/* eslint-disable no-restricted-imports */
import { ConfigPlugin, createRunOncePlugin } from 'expo/config-plugins';

import { withFingerprintProResolution } from './withFingerprintProResolution';
import { withMlKitDependenciesMerge } from './withMlKitDependenciesMerge';

const info = require('../../package.json');

const withCheckoutModulePlugin: ConfigPlugin = config => {
    config = withFingerprintProResolution(config);
    config = withMlKitDependenciesMerge(config);
    return config;
};

export default createRunOncePlugin(withCheckoutModulePlugin, info.name, info.version);
