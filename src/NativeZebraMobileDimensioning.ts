import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

export interface Spec extends TurboModule {
  EnableDimension(params: { TOKEN?: string; MODULE: string }): void;

  GetDimension(params: { TOKEN?: string; OBJECT_ID?: string }): void;
  DisableDimension(params: { TOKEN?: string }): void;
  GetDimensionParameters(params: { TOKEN?: string }): void;
  SetDimensionParameters(params: {
    TOKEN?: string;
    DIMENSIONING_UNIT?: string;
    REPORT_IMAGE?: boolean;
    TIMEOUT?: number;
  }): void;
}

export default TurboModuleRegistry.getEnforcing<Spec>(
  'ZebraMobileDimensioning'
);
