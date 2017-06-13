import React, {PropTypes} from 'react'
import {connect} from 'react-redux'
import {bindActionCreators} from 'redux'
import {defineMessages, injectIntl} from 'react-intl'

import * as HistoryActions from '../actions/HistoryActions'
import HistoryItems from '../components/HistoryItems'

const limit = 2

class UserHistory extends React.Component {
  constructor(props) {
    super(props)

    this.state = {
      hasMoreItems: true,
    }
  }
  componentWillMount () {
    this.props.historyActions.resetHistory()
    // this.props.historyActions.fetchHistory({limit:limit, offset:0})
  }

  loadItems = () => {
    console.log('loadedHistoryItems', this.props.loadedHistoryItems)
    this.props.historyActions.fetchHistory({limit:limit, offset:parseInt(this.props.loadedHistoryItems)})
    this.props.historyActions.updateHistory()
    this.props.historyActions.setNoHistoryToFetch()
  }

  render () {
    return (
      <HistoryItems historyItems={this.props.allLoadedHistory} loadItems={this.loadItems} hasMoreItems={this.props.hasMoreItems} />
    )
  }
}

UserHistory.propTypes = {
  historyActions: PropTypes.object.isRequired
}

export const messages = defineMessages({})

function mapStateToProps (state) {
  return {
    historyItems: state.history.historyData,
    allLoadedHistory: state.history.allLoadedHistory,
    loadedHistoryItems: state.history.loadedHistoryItems,
    hasMoreItems: state.history.moreToFetch
  }
}

function mapDispatchToProps (dispatch) {
  return {
    dispatch: dispatch,
    historyActions: bindActionCreators(HistoryActions, dispatch)
  }
}

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(injectIntl(UserHistory))
