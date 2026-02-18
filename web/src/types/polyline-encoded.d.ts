declare module 'polyline-encoded' {
  export function decode(encoded: string, precision?: number): [number, number][];
  export function encode(coordinates: [number, number][], precision?: number): string;
}
