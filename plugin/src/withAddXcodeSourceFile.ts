import fs from 'fs';
import path from 'path';

import { ExpoConfig } from '@expo/config';
import { ConfigPlugin, withXcodeProject } from '@expo/config-plugins';

type WithAddXcodeSourceFileProps = {
    files: string[];
};

const withAddXcodeSourceFile: ConfigPlugin<WithAddXcodeSourceFileProps> = (config: ExpoConfig, { files }) =>
    withXcodeProject(config, async config => {
        const iosProjectFolder = path.join(__dirname, '../../ios');
        const podspecsFolder = path.join(__dirname, '../../podspecs');

        files.forEach(file => {
            fs.copyFileSync(`${podspecsFolder}/${file}`, `${iosProjectFolder}/${file}`);
        });

        return config;
    });

export default withAddXcodeSourceFile;
