module.exports = {
  email: email => {
    if (!/^[^@ ]+@[^@ ]+$/i.test(email)) {
      return 'invalidEmail'
    }
  },
  ssn: ssn => {
    if (!/^\d{11,12}$/.test(ssn) || !validateSSN(ssn.toString())) {
      return 'invalidSSN'
    }
  },
  zipcode: zipcode => {
    if (!/^\d{4}$/.test(zipcode)) {
      return 'invalidZipcode'
    }
  },
  mobile: mobile => {
    if (!/^((0047)?|(\+47)?|(47)?)\d{8}$/.test(mobile)) {
      return 'invalidPhoneNumber'
    }
  },
  city: city => {
    if (!/^[A-Za-z-øæåÆØÅ]{2,}$/.test(city)) {
      return 'invalidCity'
    }
  },
  year: year => {
    if (!/^\d{4}$/.test(year) || year < 1900 || year > new Date().getFullYear() - 5) { /* What is the actual requirements for age? */
      return 'invalidYear'
    }
  },
  month: month => {
    if (!/^\d{1,2}$/.test(month) || month < 1 || month > 12) {
      return 'invalidMonth'
    }
  },
  day: day => {
    if (!/^\d{1,2}$/.test(day) || day < 1 || day > 31) {
      return 'invalidDay'
    }
  },
  pin: pin => {
    if (!/^\d{4}$/.test(pin)) {
      return 'invalidPin'
    }
  },
  repeatPin: (repeatPin, values) => {
    if (!values.pin) {
      return
    }
    if (!values.repeatPin) {
      return
    }
    if (values.pin !== values.repeatPin) {
      return 'pinsMustBeEqual'
    }
  },
  acceptTerms: acceptTerms => {
    if (!acceptTerms) {
      return 'termsMustBeAccepted'
    }
  }
}

function validateSSN (ssn) {
  if (ssn.length === 11) {
    return validateFNumber(ssn) || validateDNumber(ssn)
  } else if (ssn.length === 12) {
    return validateDUFNumber(ssn)
  } else {
    return false
  }
}

function validateFNumber (ssn) {
  if (parseInt(ssn.charAt(0)) > 3) {
    return false
  } else if (parseInt(ssn.charAt(2)) > 1) {
    return false
  } else {
    return isValidFNumberChecksum(ssn)
  }
}

function isValidFNumberChecksum (ssn) {
  let digits = []
  for (let i = 0; i < ssn.length; i++) {
    digits.push(parseInt(ssn.charAt(i)))
  }
  const multipliers1 = [ 3, 7, 6, 1, 8, 9, 4, 5, 2 ]
  let ctrl = 0
  for (let i = 0; i < multipliers1.length; i++) {
    ctrl += digits[ i ] * multipliers1[ i ]
  }
  ctrl = ctrl % 11
  let controlDigit1 = 0
  if (ctrl !== 0) {
    controlDigit1 = 11 - ctrl
  }
  if (controlDigit1 === 10) {
    return false
  }
  const multipliers2 = [ 5, 4, 3, 2, 7, 6, 5, 4, 3, 2 ]
  let ctrl2 = 0
  for (let i = 0; i < multipliers2.length - 1; i++) {
    ctrl2 += multipliers2[ i ] * digits[ i ]
  }
  ctrl2 += controlDigit1 * multipliers2[ multipliers2.length - 1 ]
  ctrl2 = ctrl2 % 11
  let controlDigit2 = 0
  if (ctrl2 !== 0) {
    controlDigit2 = 11 - ctrl2
  }
  if (controlDigit2 === 10) {
    return false
  }
  return controlDigit1 === digits[ 9 ] && controlDigit2 === digits[ 10 ]
}

function validateDNumber (ssn) {
  if (parseInt(ssn.charAt(0)) > 7 || parseInt(ssn.charAt(0) < 4)) {
    return false
  } else if (parseInt(ssn.charAt(2)) > 1) {
    return false
  } else {
    return isValidFNumberChecksum(ssn)
  }
}

function validateDUFNumber (ssn) {
  const control = parseInt(ssn.substring(10))
  const digits = ssn.substring(0, 10)
  const multipliers = [ 4, 6, 3, 2, 4, 6, 3, 2, 7, 5 ]

  let temp = 0
  for (let i = 0; i < 10; i++) {
    temp += multipliers[ i ] * parseInt(digits.charAt(i))
  }

  temp = temp % 11
  return temp === control
}
