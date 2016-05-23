import Constants from '../constants/Constants'

export function parseFilters (locationQuery) {
  const filters = []
  const filterableFields = Constants.filterableFields
  Object.keys(locationQuery).forEach(parameter => {
    if (parameter === 'filter') {
      const values = locationQuery[ parameter ] instanceof Array ? locationQuery[ parameter ] : [ locationQuery[ parameter ] ]
      values.forEach(value => {
        const split = value.split('_')
        const filterableField = filterableFields[ split[ 0 ] ]
        const aggregation = filterableField.name
        const bucket = filterableField.prefix + value.substring(`${split[ 0 ]}_`.length)
        filters.push({ aggregation: aggregation, bucket: bucket })
      })
    }
  })
  return filters
}

export function filteredSearchQuery (locationQuery) {
  let query = locationQuery.query
  let filters = parseFilters(locationQuery)

  let elasticSearchQuery = initQuery(query)
  let musts = {}
  let nestedMusts = {}

  filters.forEach(filter => {
    let path = getPath(filter.aggregation)
    if (musts[ path ]) {
      if (nestedMusts[ filter.aggregation ]) {
        nestedMusts[ filter.aggregation ].terms[ filter.aggregation ].push(filter.bucket)
      } else {
        let nestedMust = { terms: {} }
        nestedMust.terms[ filter.aggregation ] = [ filter.bucket ]
        musts[ path ].nested.query.bool.must.push(nestedMust)
        nestedMusts[ filter.aggregation ] = nestedMust
      }
    } else {
      let must = createMust(path)
      must.nested.query.bool.must[ 0 ].terms[ filter.aggregation ] = [ filter.bucket ]
      nestedMusts[ filter.aggregation ] = must.nested.query.bool.must[ 0 ]
      musts[ path ] = must
    }
  })

  Object.keys(musts).forEach(aggregation => {
    elasticSearchQuery.query.filtered.filter.bool.must.push(musts[ aggregation ])
  })

  elasticSearchQuery.size = Constants.searchQuerySize
  elasticSearchQuery.aggregations = { all: { global: {}, aggregations: {} } }

  Object.keys(Constants.filterableFields).forEach(key => {
    const field = Constants.filterableFields[ key ]
    const fieldName = field.name
    let aggregations = {
      filter: {
        and: [ elasticSearchQuery.query.filtered.query ]
      },
      aggregations: {
        [ fieldName ]: {
          nested: {
            path: getPath(fieldName)
          },
          aggregations: {
            [ fieldName ]: {
              terms: {
                field: fieldName
              }
            }
          }
        }
      }
    }

    Object.keys(musts).forEach(path => {
      let must = createMust(path)
      let nestedMusts = musts[ path ].nested.query.bool.must
      must.nested.query.bool.must = nestedMusts.filter(nestedMust => { return !nestedMust.terms[ fieldName ] })
      aggregations.filter.and.push({ bool: { must: must } })
    })

    elasticSearchQuery.aggregations.all.aggregations[ fieldName ] = aggregations
  })

  return elasticSearchQuery
}

function getPath (field) {
  return field.split('.').slice(0, -1).join('.')
}

function initQuery (query) {
  return {
    query: {
      filtered: {
        filter: {
          bool: {
            must: []
          }
        },
        query: {
          bool: {
            filter: [
              {
                simple_query_string: {
                  query: query,
                  default_operator: 'and'
                }
              }
            ],
            should: [
              {
                nested: {
                  path: 'work.contributors.agent',
                  query: {
                    multi_match: {
                      query: query,
                      fields: [ 'work.contributors.agent.name^2' ]
                    }
                  }
                }
              },
              {
                nested: {
                  path: 'work.publications',
                  query: {
                    multi_match: {
                      query: query,
                      fields: [ 'work.publications.mainTitle^2', 'work.publications.partTitle' ]
                    }
                  }
                }
              },
              {
                nested: {
                  path: 'work.subjects',
                  query: {
                    multi_match: {
                      query: query,
                      fields: [ 'work.subjects.name' ]
                    }
                  }
                }
              }
            ]
          }
        }
      }
    },
    highlight: {
      'pre_tags': [ '' ],
      'post_tags': [ '' ],
      fields: {
        'work.publications.mainTitle': {},
        'work.publications.partTitle': {},
        'work.contributors.agent.name': {}
      }
    }
  }
}

function createMust (path) {
  return {
    nested: {
      path: path,
      query: {
        bool: {
          must: [
            {
              terms: {}
            }
          ]
        }
      }
    }
  }
}
