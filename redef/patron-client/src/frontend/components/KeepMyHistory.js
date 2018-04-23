import React, { PropTypes } from 'react'
import { connect } from 'react-redux'
import { bindActionCreators } from 'redux'
import { injectIntl, defineMessages, FormattedMessage } from 'react-intl'
import * as ProfileActions from '../actions/ProfileActions'
import * as HistoryActions from '../actions/HistoryActions'

class KeepMyHistory extends React.Component {

  constructor (props) {
    super(props)
    this.handleKeepMyHistory = this.handleKeepMyHistory.bind(this)
    this.handleSaveClick = this.handleSaveClick.bind(this)
    this.state = {
      dirtyHistorySettings: false,
      checked: this.props.personalInformation.privacy === 0
    }
  }

  componentWillMount () {
    this.props.historyActions.resetHistory()
    this.props.historyActions.fetchHistory({ limit: 10, offset: 0 })
  }

  componentWillReceiveProps(props) {
    this.setState({
      dirtyHistorySettings: false,
      checked: this.props.personalInformation.privacy === 0
    })
  }

  handleKeepMyHistory (event) {
     this.setState({dirtyHistorySettings: true, checked: !this.state.checked})
  }

  handleSaveClick (event) {
    const checked = this.keepMyHistoryCheckbox.checked
    if (checked) {
      this.props.profileActions.manageHistory(0, this.props.personalAttributes.hist_cons)
    } else {
      if (this.props.historySize > 0) {
        this.props.profileActions.userHistory()
      } else {
        // user has no items in history, so we don't bother with popup
        this.props.profileActions.manageHistory(2, this.props.personalAttributes.hist_cons)
      }
    }
  }

  render () {
    return (
      <div key={Math.random()}>
        <div className="reminders-group">
          <div className="reminder-item">
            <input data-automation-id="UserSettings_keepMyHistory"
                   type="checkbox"
                   name="keep-my-history"
                   id="keep-my-history"
                   ref={e => this.keepMyHistoryCheckbox = e}
                   onChange={this.handleKeepMyHistory}
                   checked={this.state.checked} />
            <label htmlFor="keep-my-history">
                <span className="checkbox-wrapper">
                  <i className="icon-check-empty checkbox-unchecked" role="checkbox" aria-checked="false" tabIndex="0" />
                  <i className="icon-ok-squared checkbox-checked" role="checkbox" aria-checked="true" tabIndex="0" />
                </span>
                <FormattedMessage {...messages.keepMyHistory} />
            </label>
          </div>

        </div>
        <footer>
          <button className="blue-btn"
                  disabled={!this.state.dirtyHistorySettings}
                  type="button"
                  data-automation-id="UserSettings_saveConsentButton"
                  onClick={this.handleSaveClick}>
            <FormattedMessage {...messages.save} />
          </button>
          {this.props.postProfileSettingsSuccess
            ? (<span className="disclaimer">
                  <FormattedMessage {...messages.saveSuccess} />
                </span>)
            : null
          }
        </footer>
      </div>
    )
  }
}

KeepMyHistory.propTypes = {
  dispatch: PropTypes.func.isRequired,
  profileActions: PropTypes.object.isRequired,
  historyActions: PropTypes.object.isRequired,
  personalInformation: PropTypes.object.isRequired,
  personalAttributes: PropTypes.object.isRequired,
  postProfileSettingsSuccess: PropTypes.bool,
  historySize: PropTypes.number.isRequired
}

export const messages = defineMessages({
  keepMyHistory: {
    id: 'UserSettings.keepMyHistory',
    description: 'Label for checkbox which manages keep/discard history',
    defaultMessage: 'Keep my history'
  },
  myHistory: {
    id: 'UserSettings.history',
    description: 'Header for users My history',
    defaultMessage: 'My history'
  },
  saveSuccess: {
    id: 'UserSettings.saveSuccess',
    description: 'Success notification when settings are saved',
    defaultMessage: 'Changes are saved!'
  },
  save: {
    id: 'UserSettings.save',
    description: 'The label for the save button',
    defaultMessage: 'Save'
  }
})

function mapStateToProps (state) {
  return {
    postProfileSettingsSuccess: state.profile.postProfileSettingsSuccess,
    historySize: state.history.loadedHistoryItems
  }
}

function mapDispatchToProps (dispatch) {
  return {
    dispatch: dispatch,
    profileActions: bindActionCreators(ProfileActions, dispatch),
    historyActions: bindActionCreators(HistoryActions, dispatch)
  }
}

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(injectIntl(KeepMyHistory))
