import * as _ from 'lodash';

export const plainToFlattenObject = (object: any) => {
  const result = {};

  function flatten(obj: any, prefix = ''): any {
    _.forEach(obj, (value, key) => {
      if (_.isObject(value)) {
        flatten(value, `${prefix}${key}.`);
      } else {
        result[`${prefix}${key}`] = value;
      }
    });
  }
  flatten(object);

  return result;
};
