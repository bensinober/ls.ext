import React, { PropTypes } from 'react'
import { reduxForm } from 'redux-form'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { injectIntl, intlShape, defineMessages, FormattedMessage } from 'react-intl'

import * as RegistrationActions from '../../actions/RegistrationActions'
import * as ModalActions from '../../actions/ModalActions'
import ValidationMessage from '../../components/ValidationMessage'
import fields from '../../../common/forms/registrationPartOne'
import validator from '../../../common/validation/validator'
import asyncValidate from '../../utils/asyncValidate'
import FormInputField from '../../components/FormInputField'

const formName = 'registrationPartOne'

class RegistrationFormPartOne extends React.Component {
  constructor (props) {
    super(props)
    this.handleCancel = this.handleCancel.bind(this)
  }

  handleCancel (event) {
    event.preventDefault()
    this.props.modalActions.hideModal()
  }

  renderSSNInfo () {
    return (
      <p data-automation-id="ssninfo">
        <FormattedMessage {...messages.ssnInfo} />
      </p>
    )
  }

  renderCheckingForExistingUser () {
    return (
      <p data-automation-id="checking_existing_user">
        <FormattedMessage {...messages.checkingForExistingUser} />
      </p>
    )
  }

  renderCheckForExistingUserSuccess () {
    return (
      <p data-automation-id="check_for_existing_user_success">
        <FormattedMessage {...messages.checkForExistingUserSuccess} />
      </p>
    )
  }

  renderCheckForExistingUserError (message) {
    return (
      <div data-automation-id="check_for_existing_user_error">
        <p data-automation-id="check_for_existing_user_error_message">
          {messages[ message ]
            ? <FormattedMessage {...messages[ message ]} />
            : <FormattedMessage {...messages.genericRegistrationError} />}
        </p>
      </div>
    )
  }

  renderContinueAndCancelButtons (submitting) {
    return (
      <p>
        <button className="black-btn" type="submit" disabled={submitting || this.hasInvalidFormFields()}
                data-automation-id="check_existing_user_button">
          <FormattedMessage {...messages.checkForExistingUser} />
        </button>

        <a className="cancel-link" onClick={this.handleCancel} title="cancel"><FormattedMessage {...messages.cancel} /></a>
      </p>
    )
  }

  getValidator (field) {
    if (field.meta.touched && field.meta.error) {
      return <div className="feedback"><ValidationMessage message={field.meta.error} /></div>
    }
  }

  hasInvalidFormFields () {
    return Object.values(this.props.fields).every(field => field.error)
  }

  isNotEmpty () {
    return Object.values(this.props.fields).every(field => field.touched)
  }

  render () {
    const { submitting } = this.props

    return (
      <form onSubmit={this.props.handleSubmit(this.props.registrationActions.checkForExistingUser)}>
        <h1><FormattedMessage {...messages.registerAsLoaner} /></h1>
        <fieldset disabled={this.props.checkForExistingUserSuccess}>
          <legend><FormattedMessage {...messages.nameLabel} /></legend>
          <FormInputField name="firstName" message={messages.firstName} formName={formName} />
          <FormInputField name="lastName" message={messages.lastName} formName={formName} getValidator={this.getValidator} />
        </fieldset>
        <fieldset disabled={this.props.checkForExistingUserSuccess}>
          <legend><FormattedMessage {...messages.birthdate} /></legend>
          <div className="date-of-birth">
            <FormInputField name="day" message={messages.day} formName={formName} getValidator={this.getValidator} />
            <FormInputField name="month" message={messages.month} formName={formName} getValidator={this.getValidator} />
            <FormInputField name="year" message={messages.year} formName={formName} getValidator={this.getValidator} />
          </div>
        </fieldset>
        <fieldset disabled={this.props.checkForExistingUserSuccess}>
          <legend><FormattedMessage {...messages.ssnHeader} /></legend>
          <p>
            <a onClick={this.props.registrationActions.showSSNInfo} title="ssnLink">
              <FormattedMessage {...messages.ssnLink} />
            </a>
          </p>
          {this.props.showSSNInfo ? this.renderSSNInfo() : ''}
          <FormInputField name="ssn" message={messages.ssn} formName={formName} getValidator={this.getValidator} />
          {this.props.isCheckingForExistingUser ? this.renderCheckingForExistingUser() : ''}
          {/* TODO: also handle all fields empty */}
          {this.props.checkForExistingUserSuccess ? null : this.renderContinueAndCancelButtons(submitting)}
        </fieldset>
        {this.props.checkForExistingUserSuccess ? this.renderCheckForExistingUserSuccess() : ''}
        {this.props.checkForExistingUserFailure ? this.renderCheckForExistingUserError(this.props.registrationError) : ''}
      </form>
    )
  }
}

const messages = defineMessages({
  genericRegistrationError: {
    id: 'RegistrationFormPartOne.genericRegistrationError',
    description: 'Error message when registering',
    defaultMessage: 'Some error occurred during registering. Please try again later.'
  },
  checkForExistingUser: {
    id: 'RegistrationFormPartOne.checkForExistingUser',
    description: 'The user validation button in registration form',
    defaultMessage: 'Continue'
  },
  checkingForExistingUser: {
    id: 'RegistrationFormPartOne.checkingForExistingUser',
    description: 'The message when checking against already registered users',
    defaultMessage: 'Checking agains existing user base...'
  },
  checkForExistingUserSuccess: {
    id: 'RegistrationFormPartOne.checkForExistingUserSuccess',
    description: 'The message when check against already registered users succeeds',
    defaultMessage: 'No existing user found, please continue registering a new user.'
  },
  userExistsInLocalDB: {
    id: 'RegistrationFormPartOne.userExistsInLocalDB',
    description: 'Message displayed to user when already registered locally',
    defaultMessage: 'User seems to be already registered at this library. Please contact library to get credentials'
  },
  userExistsInCentralDB: {
    id: 'RegistrationFormPartOne.userExistsInCentralDB',
    description: 'Message displayed to user when already registered in the Norwegian Patron DB',
    defaultMessage: 'User seems to be already registered in the Norwegian Patron Database. Please contact library to get credentials'
  },
  cancel: {
    id: 'RegistrationFormPartOne.cancel',
    description: 'The cancel button text',
    defaultMessage: 'Cancel'
  },
  registerAsLoaner: {
    id: 'RegistrationFormPartOne.registerAsLoaner',
    description: 'The header text of the modal dialog',
    defaultMessage: 'Register as loaner'
  },
  nameLabel: {
    id: 'RegistrationFormPartOne.nameLabel',
    description: 'Label for the fieldset (legend) names',
    defaultMessage: 'Name'
  },
  firstName: {
    id: 'RegistrationFormPartOne.firstName',
    description: 'Label for the first name field',
    defaultMessage: 'First name'
  },
  lastName: {
    id: 'RegistrationFormPartOne.lastName',
    description: 'Label for the last name field',
    defaultMessage: 'Last name'
  },
  personInfoLegend: {
    id: 'RegistrationFormPartOne.personInfoLegend',
    description: 'Fieldset legend for personal information',
    defaultMessage: 'Personal information'
  },
  birthdate: {
    id: 'RegistrationFormPartOne.birthdate',
    description: 'Label for birthdate',
    defaultMessage: 'Birthdate'
  },
  day: {
    id: 'RegistrationFormPartOne.day',
    description: 'Label for the day field',
    defaultMessage: 'Day'
  },
  month: {
    id: 'RegistrationFormPartOne.month',
    description: 'Label for the month field',
    defaultMessage: 'Month'
  },
  year: {
    id: 'RegistrationFormPartOne.year',
    description: 'Label for the year field',
    defaultMessage: 'Year'
  },
  ssnHeader: {
    id: 'RegistrationFormPartOne.ssnHeader',
    description: 'Header for input field social security number',
    defaultMessage: 'Social security number'
  },
  ssn: {
    id: 'RegistrationFormPartOne.ssnSpec',
    description: 'Specification of social security number',
    defaultMessage: 'Personnr./D-nr./DUF-nr'
  },
  ssnLabel: {
    id: 'RegistrationFormPartOne.ssnLabel',
    description: 'Label(s) for social security number',
    defaultMessage: 'SSN/D-nr./DUF.nr.'
  },
  ssnLink: {
    id: 'RegistrationFormPartOne.ssnLink',
    description: 'Link label for info on ssn',
    defaultMessage: 'Why do I need to fill in birth date and Social security number?'
  },
  ssnInfo: {
    id: 'RegistrationFormPartOne.ssnInfo',
    description: 'Expanded info on ssn',
    defaultMessage: 'SSN is your personal Social security number. It is either ... etc'
  }
})

RegistrationFormPartOne.propTypes = {
  dispatch: PropTypes.func.isRequired,
  modalActions: PropTypes.object.isRequired,
  registrationActions: PropTypes.object.isRequired,
  fields: PropTypes.object,
  handleSubmit: PropTypes.func.isRequired,
  submitting: PropTypes.bool.isRequired,
  asyncValidating: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.bool
  ]).isRequired,
  username: PropTypes.string,
  message: PropTypes.string,
  isError: PropTypes.bool,
  isSuccess: PropTypes.bool,
  isCheckingForExistingUser: PropTypes.bool,
  checkForExistingUserSuccess: PropTypes.bool,
  checkForExistingUserFailure: PropTypes.bool,
  registrationError: PropTypes.string,
  showSSNInfo: PropTypes.bool,
  intl: intlShape.isRequired
}

function mapStateToProps (state) {
  return {
    libraries: state.application.libraries,
    showSSNInfo: state.registration.showSSNInfo,
    isCheckingForExistingUser: state.registration.isCheckingForExistingUser,
    checkForExistingUserSuccess: state.registration.checkForExistingUserSuccess,
    checkForExistingUserFailure: state.registration.checkForExistingUserFailure,
    registrationError: state.registration.registrationError,
    initialValues: {},
    fields: state.form.registrationPartOne ? state.form.registrationPartOne : {}
  }
}

function mapDispatchToProps (dispatch) {
  return {
    dispatch: dispatch,
    registrationActions: bindActionCreators(RegistrationActions, dispatch),
    modalActions: bindActionCreators(ModalActions, dispatch)
  }
}

const intlRegistrationFormPartOne = injectIntl(RegistrationFormPartOne)
export { intlRegistrationFormPartOne as RegistrationFormPartOne }

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(reduxForm({
  form: formName,
  asyncValidate,
  asyncBlurFields: Object.keys(fields).filter(field => fields[ field ].asyncValidation),
  validate: validator(fields)
})(intlRegistrationFormPartOne))
