import ZebraMobileDimensioning from './NativeZebraMobileDimensioning';

export function EnableDimension(params: {
  TOKEN?: string;
  MODULE: string;
}): void {
  ZebraMobileDimensioning.EnableDimension(params);
}

export function GetDimension(params: {
  TOKEN?: string;
  OBJECT_ID?: string;
}): void {
  ZebraMobileDimensioning.GetDimension(params);
}

export function DisableDimension(params: { TOKEN?: string }): void {
  ZebraMobileDimensioning.DisableDimension(params);
}

export function GetDimensionParameters(params: { TOKEN?: string }): void {
  ZebraMobileDimensioning.GetDimensionParameters(params);
}

export function SetDimensionParameters(params: {
  TOKEN?: string;
  DIMENSIONING_UNIT?: string;
  REPORT_IMAGE?: boolean;
  TIMEOUT?: number;
}): void {
  ZebraMobileDimensioning.SetDimensionParameters(params);
}
