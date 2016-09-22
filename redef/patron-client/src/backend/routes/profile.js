const bodyParser = require('body-parser')
const jsonParser = bodyParser.json()
const userSettingsMapper = require('../utils/userSettingsMapper')
const bcrypt = require('bcrypt-nodejs')
const userInfoForm = require('../../common/forms/userInfoForm')
const extendedValidator = require('../utils/extendedValidator')(userInfoForm)

module.exports = (app) => {
  const fetch = require('../fetch')(app)
  app.get('/api/v1/profile/info', (request, response) => {
    fetch(`http://xkoha:8081/api/v1/patrons/${request.session.borrowerNumber}`)
      .then(res => {
        if (res.status === 200) {
          return res.json()
        } else {
          response.status(res.status).send(res.statusText)
          throw Error()
        }
      }).then(json => response.status(200).send(parsePatron(json)))
      .catch(error => {
        console.log(error)
        response.sendStatus(500)
      })
  })

  app.post('/api/v1/profile/info', jsonParser, (request, response) => {
    const errors = extendedValidator(request.body)
    if (Object.keys(errors).length > 0) {
      response.status(400).send({ errors: errors })
      return
    }

    const patron = {
      address: request.body.address,
      zipcode: request.body.zipcode,
      city: request.body.city,
      country: request.body.country,
      smsalertnumber: request.body.mobile,
      phone: request.body.telephone,
      email: request.body.email
    }

    fetch(`http://xkoha:8081/api/v1/patrons/${request.session.borrowerNumber}`, {
      method: 'PUT',
      body: JSON.stringify(patron)
    }).then(res => {
      if (res.status === 200 || res.status === 204) {
        res.json().then(json => response.status(res.status).send(parsePatron(json)))
      } else {
        response.status(res.status).send(res.body)
      }
    })
      .catch(error => {
        console.log(error)
        response.sendStatus(500)
      })
  })

  app.get('/api/v1/profile/loans', (request, response) => {
    Promise.all([ fetchAllCheckouts(request), fetchAllHoldsAndPickups(request) ])
      .then(([checkouts, holdsAndPickups]) => {
        const holds = holdsAndPickups.filter(item => item.status !== 'W')
        const pickups = holdsAndPickups.filter(item => item.status === 'W')
        response.send({
          name: `${request.session.borrowerName}`,
          loans: checkouts,
          reservations: holds,
          pickups: pickups
        })
      })
  })

  app.get('/api/v1/profile/checkouts', (request, response) => {
    fetchAllCheckouts(request)
      .then(res => {
        console.log(res)
        response.status(200).send(res)
      })
      .catch(error => {
        console.log(error)
        response.sendStatus(500)
      })
  })

  app.get('/api/v1/profile/holdsandpickups', (request, response) => {
    fetchAllHoldsAndPickups(request)
      .then(res => {
        console.log(res)
        response.status(200).send(res)
      })
      .catch(error => {
        console.log(error)
        response.sendStatus(500)
      })
  })

  function fetchAllHoldsAndPickups (request) {
    return fetch(`http://xkoha:8081/api/v1/holds?borrowernumber=${request.session.borrowerNumber}`)
      .then(res => {
        if (res.status === 200) {
          return res.json()
        } else {
          throw Error(res.statusText)
        }
      }).then(json => {
        const promises = json.map(hold => fetchHoldFromBiblioNumber(hold, request))
        return Promise.all(promises)
      }).then(holds => {
        return holds
      })
  }

  function fetchHoldFromBiblioNumber (hold, request) {
    return fetch(`http://xkoha:8081/api/v1/biblios/${hold.biblionumber}`)
      .then(res => {
        if (res.status === 200) {
          return res.json()
        } else {
          throw Error(res.statusText)
        }
      }).then(json => {
        const pickupNumber = hold.waitingdate ? `${hold.waitingdate.split('-')[ 2 ]}/${hold.reserve_id}` : 'unknown'
        const waitingPeriod = hold.found === 'T' ? '1-2 dager' : 'cirka 2-4 uker'
        const expiry = hold.waitingdate ? new Date(Date.parse(`${hold.waitingdate}`) + (1000 * 60 * 60 * 24 * 7)).toISOString(1).split('T')[ 0 ] : 'unknown'
        return {
          recordId: hold.biblionumber,
          reserveId: hold.reserve_id,
          title: json.title,
          author: json.author,
          publicationYear: json.publicationYear,
          orderedDate: hold.reservedate,
          branchCode: hold.branchcode,
          status: hold.found,
          waitingDate: hold.waitingDate,
          expiry: expiry,
          waitingPeriod: waitingPeriod,
          pickupNumber: pickupNumber
        }
      })
  }

  function fetchAllCheckouts (request) {
    return fetch(`http://xkoha:8081/api/v1/checkouts?borrowernumber=${request.session.borrowerNumber}`)
      .then(res => {
        if (res.status === 200) {
          return res.json()
        } else {
          throw Error(res.statusText)
        }
      }).then(json => {
        const promises = json.map(loan => fetchLoanFromItemNumber(loan, request))
        return Promise.all(promises)
      }).then(loans => {
        return loans
      })
  }

  function fetchLoanFromItemNumber (loan, request) {
    return fetch(`http://xkoha:8081/api/v1/items/${loan.itemnumber}/biblio`)
      .then(res => {
        if (res.status === 200) {
          return res.json()
        } else {
          throw Error(res.statusText)
        }
      }).then(json => {
        return {
          itemNumber: loan.itemnumber,
          recordId: json.biblionumber,
          dueDate: loan.date_due,
          title: json.title,
          author: json.author,
          publicationYear: json.publicationyear,
          checkoutId: loan.issue_id
        }
      })
  }

  app.post('/api/v1/profile/settings', jsonParser, (request, response) => {
    fetch(`http://xkoha:8081/api/v1/messagepreferences/${request.session.borrowerNumber}`, {
      method: 'PUT',
      headers: {
        'Content-type': 'application/json'
      },
      body: JSON.stringify(userSettingsMapper.patronSettingsToKohaSettings(request.body))
    }).then(res => {
      if (res.status === 200) {
        return res.json()
      } else {
        response.status(res.status).send(res.body)
      }
    }).then(kohaSettings => response.status(200).send(userSettingsMapper.kohaSettingsToPatronSettings(kohaSettings)))
      .catch(error => {
        console.log(error)
        response.sendStatus(500)
      })
  })

  app.put('/api/v1/profile/settings/password', jsonParser, (request, response) => {
    if (bcrypt.compareSync(request.body.currentPassword, request.session.passwordHash)) {
      fetch(`http://xkoha:8081/api/v1/patrons/${request.session.borrowerNumber}`, {
        method: 'PUT',
        headers: {
          'Content-type': 'application/json'
        },
        body: JSON.stringify({ password: request.body.newPassword })
      }).then(res => {
        console.log(res.status)
        if (res.status === 200) {
          request.session.passwordHash = bcrypt.hashSync(request.body.newPassword)
          response.sendStatus(200)
        } else {
          response.status(res.status).send(res.body)
        }
      }).catch(error => {
        console.log(error)
        response.sendStatus(500)
      })
    } else {
      response.status(403).send('Old password is not correct')
    }
  })

  app.get('/api/v1/profile/settings', (request, response) => {
    return fetch(`http://xkoha:8081/api/v1/messagepreferences/${request.session.borrowerNumber}`)
      .then(res => {
        if (res.status === 200) {
          return res.json()
        } else {
          throw Error(res.statusText)
        }
      }).then(kohaSettings => userSettingsMapper.kohaSettingsToPatronSettings(kohaSettings))
      .then(patronSettings => response.send(patronSettings))
      .catch(error => {
        console.log(error)
        response.sendStatus(500)
      })
  })

  function parsePatron (patron) {
    return {
      borrowerNumber: patron.borrowernumber || '',
      name: `${patron.firstname} ${patron.surname}`,
      address: patron.address || '',
      zipcode: patron.zipcode || '',
      city: patron.city || '',
      country: patron.country || '',
      mobile: patron.smsalertnumber || '', // is this the only sms number?
      telephone: patron.phone || '',
      email: patron.email || '',
      birthdate: patron.dateofbirth || '',
      loanerCardIssued: patron.dateenrolled || '',
      loanerCategory: patron.categorycode || '',
      lastUpdated: '2016-02-01'
    }
  }
}
