import React, { PropTypes } from 'react'
import NonIETransitionGroup from './NonIETransitionGroup'
import { defineMessages, FormattedMessage } from 'react-intl'
import InfiniteScroll from 'react-infinite-scroller'

import HistoryItem from './HistoryItem'

class HistoryItems extends React.Component {
  render () {
    const items = this.props.historyItems.map((el, i) => <HistoryItem key={i} historyItem={el} />)
    return (
      <section
        className="history"
        style={{ marginTop: '1em' }}>
        {this.props.historyItems.length > 0 ?
          <div className="delete-history-buttons">
            <button className="small-blue-btn">Slett valgte</button> &nbsp; <button onClick={this.props.historyActions.deleteAllHistory}className="small-blue-btn">Slett alle</button>
          </div>
          : null }
        <h1><FormattedMessage {...messages.history} /></h1>
        <InfiniteScroll
          pageStart={0}
          loadMore={this.props.loadItems}
          hasMore={this.props.hasMoreItems}
          threshold={10}
          loader={<div className="loader"><FormattedMessage {...messages.loading} /></div>}
        >
          {items}
        </InfiniteScroll>
      </section>
    )
  }
}

HistoryItems.propTypes = {
  historyItems: PropTypes.array.isRequired,
  loadItems: PropTypes.func.isRequired,
  hasMoreItems: PropTypes.bool.isRequired,
  historyActions: PropTypes.object.isRequired
}

export const messages = defineMessages({
  history: {
    id: 'History.history',
    description: 'The label of history title',
    defaultMessage: 'My history'
  },
  loading: {
    id: 'History.loading',
    description: 'Message to show when loading history items',
    defaultMessage: 'Loading ...'
  }
})

export default HistoryItems
