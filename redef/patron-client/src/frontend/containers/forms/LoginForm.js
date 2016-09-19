/**
 * Created by Nikolai on 12/09/16.
 */
import React, { PropTypes } from 'react'
import { reduxForm } from 'redux-form'
import { connect } from 'react-redux'
import { injectIntl, intlShape, FormattedMessage } from 'react-intl'
import { bindActionCreators } from 'redux'

import FormInputFieldPlain from '../../components/FormInputFieldPlain'
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

  render () {
    return (
      <div data-automation-id="login_modal">
        <form onSubmit={this.handleLogin}>
          <h1>Logg inn for å reservere</h1>
          <FormInputFieldPlain name="username" type="text" message={this.props.messages.username} headerType="h2"
                               formName={formName} />
          <FormInputFieldPlain name="password" type="password" message={this.props.messages.password} headerType="h2"
                               formName={formName} />

          <p>Glemt PIN-kode?</p>
          <button className="black-btn" type="submit" disabled={this.props.isRequestingLogin}
                  onClick={this.handleLogin}
                  data-automation-id="login_button">
            <FormattedMessage {...this.props.messages.logIn} />
          </button>
          <h3>Er du ikke registrert? <a data-automation-id="registration_link" onClick={this.handleRegistrationClick}
                                        title="register">Registrer deg</a></h3>
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