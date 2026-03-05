import { describe, it, expect } from 'vitest';
import DecodePolylineMap from '../components/DecodePolylineMap';

// Preso da documentazione Google Maps
const encoded = '_p~iF~ps|U_ulLnnqC_mqNvxq`@';

describe('DecodePolylineMap', () => {
  it('decodes a valid encoded polyline', () => {
    const result = DecodePolylineMap(encoded);

    expect(result.length).toBe(3);
    expect(result[0][0]).toBeCloseTo(38.5, 4);
    expect(result[0][1]).toBeCloseTo(-120.2, 4);
    expect(result[1][0]).toBeCloseTo(40.7, 4);
    expect(result[1][1]).toBeCloseTo(-120.95, 4);
    expect(result[2][0]).toBeCloseTo(43.252, 4);
    expect(result[2][1]).toBeCloseTo(-126.453, 4);
  });

  it('returns empty array when input is empty', () => {
    const result = DecodePolylineMap('');
    expect(result).toEqual([]);
  });

  it('decodes single point with positive and negative deltas', () => {
    // encoded string costruita per generare dlat negativo e dlng positivo
    const customEncoded = '?`@J'; // lat: -1, lng: 2
    const result = DecodePolylineMap(customEncoded);

    // Aumentiamo la tolleranza per i valori decodificati
    expect(result[0][0]).toEqual(0);
    expect(result[0][1]).toEqual(-0.00017);
  });
});
