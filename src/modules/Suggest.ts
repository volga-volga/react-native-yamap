import NativeSuggestModule, {type SuggestOptions, type YamapSuggest} from '../spec/NativeSuggestsModule';
import {type Point} from "../";

const suggest = (query: string, options?: SuggestOptions) => {
  if (options) {
    return NativeSuggestModule.suggestWithOptions(query, options);
  }
  return NativeSuggestModule.suggest(query);
};

const suggestWithCoords = async (query: string, options?: SuggestOptions) => {
  const suggests = await suggest(query, options);

  return suggests.map((item) => ({
    ...item,
    ...getCoordsFromSuggest(item),
  }));
};

const getCoordsFromSuggest = (yamapSuggest: YamapSuggest): Point | undefined => {
  const coords = yamapSuggest.uri
    ?.toString()
    ?.split('?')[1]
    ?.split('&')
    ?.find((param) => param.startsWith('ll'))
    ?.toString()
    ?.split('=')[1];

  if (!coords) {
    return;
  }

  const coordsArr = coords.split('%2C');

  return {
    lat: +coordsArr[1],
    lon: +coordsArr[0],
  };
};

export const Suggest = {
  suggest,
  suggestWithCoords,
  reset: NativeSuggestModule.reset,
  getCoordsFromSuggest,
};
