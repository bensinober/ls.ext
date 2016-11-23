import React, { PropTypes } from 'react'
import NonIETransitionGroup from './NonIETransitionGroup'
import { injectIntl, intlShape, defineMessages, FormattedMessage } from 'react-intl'
import AgeLimit from './work/fields/publication/AgeLimit'
import BiblioNumber from './work/fields/publication/BiblioNumber'
import Binding from './work/fields/publication/Binding'
import Contributors from './work/fields/Contributors'
import Duration from './work/fields/publication/Duration'
import Ean from './work/fields/publication/Ean'
import Edition from './work/fields/publication/Edition'
import FormatAdaptations from './work/fields/publication/FormatAdaptations'
import Formats from './work/fields/publication/Formats'
import Isbn from './work/fields/publication/Isbn'
import NumberOfPages from './work/fields/publication/NumberOfPages'
import PlaceOfPublication from './work/fields/publication/PlaceOfPublication'
import Publishers from './work/fields/publication/Publishers'
import PublisherSeries from './work/fields/publication/PublisherSeries'
import SerialIssues from './work/fields/publication/SerialIssues'
import Subtitles from './work/fields/publication/Subtitles'

class PublicationInfo extends React.Component {
  renderLocation (location) {
    if (location && location !== '') {
      return <span className="item-location">{location}</span>
    }
  }

  renderItems (items) {
    if (items) {
      items.sort((a, b) => {
        // Sort items by branch, shelfmark, and "Ikke til hjemlån"-status
        const keya = `${this.props.intl.formatMessage({ id: a.branchcode })}${a.shelfmark}${a.notforloan}`
        const keyb = `${this.props.intl.formatMessage({ id: b.branchcode })}${b.shelfmark}${b.notforloan}`
        if (keya > keyb) {
          return 1
        }
        if (keya < keyb) {
          return -1
        }
        return 0
      })
      return (
        <NonIETransitionGroup
          transitionName="fade-in"
          transitionAppear
          transitionAppearTimeout={500}
          transitionEnterTimeout={500}
          transitionLeaveTimeout={500}
          component="table"
          className="test">
          <thead>
          <tr>
            <th><FormattedMessage {...messages.branch} /></th>
            <th><FormattedMessage {...messages.shelfmark} /></th>
            <th><FormattedMessage {...messages.status} /></th>
          </tr>
          </thead>
          <tbody data-automation-id="work_items">
          {items.map(item => {
            return (
              <tr key={item.barcode} className={(item.available > 0) ? 'available' : 'not-available'}>
                <td data-automation-id="item_location">{this.props.intl.formatMessage({ id: item.branchcode })}</td>
                <td data-automation-id="item_shelfmark">{item.shelfmark} {this.renderLocation(item.location)}</td>
                <td data-automation-id="item_status">{this.renderAvailability(item)}</td>
              </tr>
            )
          })}
          </tbody>
        </NonIETransitionGroup>
      )
    }
  }

  renderAvailability (item) {
    if (item.notforloan) {
      return (
        <span>
          {item.total} <FormattedMessage {...messages.available} /> - <FormattedMessage {...messages.onlyInhouse} />
        </span>
      )
    } else {
      return (
        <span>
          {item.available} <FormattedMessage {...messages.of} /> {item.total}
          <FormattedMessage {...messages.available} />
        </span>
      )
    }
  }

  render () {
    const { publication, publication: { items } } = this.props
    return (
      <NonIETransitionGroup
        transitionName="fade-in"
        transitionAppear
        transitionAppearTimeout={500}
        transitionEnterTimeout={500}
        transitionLeaveTimeout={500}
        component="section"
        className="publication-info"
        data-automation-id={`publication_info_${publication.uri}`}>
        <div className="left">
          <AgeLimit ageLimit={publication.ageLimit} />
          <Contributors contributors={publication.contributors} />
          <NumberOfPages numberOfPages={publication.numberOfPages} />
          <Edition edition={publication.edition} />
          <Publishers publishers={publication.publishers} />
          <PlaceOfPublication placeOfPublication={publication.placeOfPublication} />
          <Subtitles subtitles={publication.subtitles} />
          <SerialIssues serialIssues={publication.serialIssues} />
        </div>
        <div className="right">
          <Formats formats={publication.formats} />
          <Isbn isbn={publication.isbn} />
          <BiblioNumber biblioNumber={publication.recordId} />
          <PublisherSeries publisherSeries={publication.publisherSeries} />
          <FormatAdaptations formatAdaptations={publication.formatAdaptations} />
          <Binding binding={publication.binding} />
          <Duration duration={publication.duration} />
          <Ean ean={publication.ean} />
        </div>
        <div className="wide">
          <div className="meta-label"><FormattedMessage {...messages.note} /></div>
          <p className="note">{publication.description}</p>
        </div>
        <div className="entry-items">
          <h2><FormattedMessage {...messages.items} /></h2>
          {this.renderItems(items)}
        </div>
      </NonIETransitionGroup>
    )
  }
}

PublicationInfo.propTypes = {
  publication: PropTypes.object.isRequired,
  expandSubResource: PropTypes.func.isRequired,
  intl: intlShape.isRequired
}

export const messages = defineMessages({
  about: {
    id: 'PublicationInfo.about',
    description: 'Heading for the publication info',
    defaultMessage: 'About this publication'
  },
  items: {
    id: 'PublicationInfo.items',
    description: 'Heading for items',
    defaultMessage: 'Items'
  },
  note: {
    id: 'PublicationInfo.note',
    description: 'Header for the note column',
    defaultMessage: 'Note:'
  },
  of: {
    id: 'PublicationInfo.of',
    description: 'Used as component for availability',
    defaultMessage: 'of'
  },
  available: {
    id: 'PublicationInfo.available',
    description: 'Used as component for availability',
    defaultMessage: 'available'
  },
  status: {
    id: 'PublicationInfo.status',
    description: 'Table header for publication items table',
    defaultMessage: 'Status'
  },
  shelfmark: {
    id: 'PublicationInfo.shelfmark',
    description: 'Table header for publication items table',
    defaultMessage: 'Shelfmark'
  },
  branch: {
    id: 'PublicationInfo.branch',
    description: 'Table header for publication items table',
    defaultMessage: 'Branch'
  },
  onlyInhouse: {
    id: 'PublicationInfo.onlyInhouse',
    description: 'Label stating that item can only be used in the library',
    defaultMessage: 'Only for use in the library'
  }
})

export default injectIntl(PublicationInfo)
