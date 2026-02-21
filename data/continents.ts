export interface ContinentData {
  id: string;
  name: string;
  bonus: number;
  territoryIds: string[];
}

export const continents: ContinentData[] = [
  {
    id: 'north-america',
    name: 'North America',
    bonus: 5,
    territoryIds: ['alaska', 'northwest-territory', 'greenland', 'alberta', 'ontario', 'quebec', 'western-us', 'eastern-us', 'central-america'],
  },
  {
    id: 'south-america',
    name: 'South America',
    bonus: 2,
    territoryIds: ['venezuela', 'peru', 'brazil', 'argentina'],
  },
  {
    id: 'europe',
    name: 'Europe',
    bonus: 5,
    territoryIds: ['iceland', 'scandinavia', 'great-britain', 'northern-europe', 'western-europe', 'southern-europe', 'ukraine'],
  },
  {
    id: 'africa',
    name: 'Africa',
    bonus: 3,
    territoryIds: ['north-africa', 'egypt', 'east-africa', 'congo', 'south-africa', 'madagascar'],
  },
  {
    id: 'asia',
    name: 'Asia',
    bonus: 7,
    territoryIds: ['ural', 'siberia', 'yakutsk', 'kamchatka', 'irkutsk', 'mongolia', 'japan', 'afghanistan', 'china', 'india', 'siam', 'middle-east'],
  },
  {
    id: 'australia',
    name: 'Australia',
    bonus: 2,
    territoryIds: ['indonesia', 'new-guinea', 'western-australia', 'eastern-australia'],
  },
];
