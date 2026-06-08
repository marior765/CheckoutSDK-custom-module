import { ExpoConfig } from 'expo/config';

import { ConfigPlugin, withPodfile } from '@expo/config-plugins';
import { mergeContents } from '@expo/config-plugins/build/utils/generateCode';

type WithCustomPodsProps = {
    pods: string[];
};

const withCustomPods: ConfigPlugin<WithCustomPodsProps> = (config: ExpoConfig, { pods }) =>
    withPodfile(config, async config => {
        config.modResults.contents = mergeContents({
            tag: 'custom-pods',
            src: config.modResults.contents,
            newSrc: pods.join('\n'),
            anchor: /use_native_modules/,
            offset: 0,
            comment: '#',
        }).contents;

        return config;
    });

export default withCustomPods;
