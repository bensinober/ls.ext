/**
 * Created by Nikolai on 12/09/16.
 */
import React, { PropTypes } from 'react'
import { reduxForm } from 'redux-form'
import { connect } from 'react-redux'
import { injectIntl, intlShape, FormattedMessage } from 'react-intl'
import { bindActionCreators } from 'redux'
import { Link } from 'react-router'

import FormInputField from '../../components/FormInputField'
import * as RegistrationActions from '../../actions/RegistrationActions'
import * as LoginActions from '../../actions/LoginActions'
import * as ModalActions from '../../actions/ModalActions'

const formName = 'loginForm'

class LoginForm extends React.Component {
  constructor (props) {
    super(props)
    this.handleLogin = this.handleLogin.bind(this)
    this.handleRegistrationClick = this.handleRegistrationClick.bind(this)
  }

  handleLogin (event) {
    event.preventDefault()
    this.props.loginActions.login(
      this.props.fields.values.username,
      this.props.fields.values.password,
      this.props.successAction ? [ ModalActions.hideModal(), this.props.successAction ] : [ ModalActions.hideModal() ]
    )
  }

  handleRegistrationClick (event) {
    event.preventDefault()
    this.props.registrationActions.startRegistration()
  }

  closeModal () {
    ModalActions.hideModal()
  }

  render () {
    return (
      <div data-automation-id="login_modal">

        <button className="close-modal-button" onClick={this.props.modalActions.hideModal}>
          <i className="icon-cancel" />
        </button>

        <form onSubmit={this.handleLogin}>
          <h1>Logg inn for å reservere</h1>
          <FormInputField name="username" message={this.props.messages.username} type="text" formName={formName} />
          <FormInputField name="password" message={this.props.messages.password} type="password" formName={formName} />
          <p className="forgot-pin">
            <Link to="#">Glemt PIN-kode?</Link>
          </p>
          <p>
            <button className="black-btn" type="submit" disabled={this.props.isRequestingLogin}
                    onClick={this.handleLogin}
                    data-automation-id="login_button">
              <FormattedMessage {...this.props.messages.logIn} />
            </button>
            <Link to="#" className="cancel-link" onClick={this.props.modalActions.hideModal}>
              Avbryt
            </Link>
          </p>
          <p>
            Er du ikke registrert? <br />
            <a data-automation-id="registration_link" onClick={this.handleRegistrationClick} title="register">Registrer deg</a>
          </p>
        </form>
      </div>
    )
  }
}

LoginForm.propTypes = {
  dispatch: PropTypes.func.isRequired,
  intl: intlShape.isRequired,
  isRequestingLogin: PropTypes.bool.isRequired,
  messages: PropTypes.object.isRequired,
  successAction: PropTypes.object,
  registrationActions: PropTypes.object.isRequired,
  loginActions: PropTypes.object.isRequired,
  fields: PropTypes.object.isRequired,
  modalActions: PropTypes.object.isRequired
}

function mapStateToProps (state) {
  return {
    isRequestingLogin: state.application.isRequestingLogin,
    fields: state.form.loginForm ? state.form.loginForm : {}
  }
}

function mapDispatchToProps (dispatch) {
  return {
    dispatch: dispatch,
    registrationActions: bindActionCreators(RegistrationActions, dispatch),
    loginActions: bindActionCreators(LoginActions, dispatch),
    modalActions: bindActionCreators(ModalActions, dispatch)
  }
}

const intlLoginForm = injectIntl(LoginForm)

export { intlLoginForm as LoginForm }

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(reduxForm({ form: formName })(intlLoginForm))
