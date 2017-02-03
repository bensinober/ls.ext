import React, { PropTypes } from 'react'
import { defineMessages, FormattedMessage } from 'react-intl'
import { Link } from 'react-router'

class Genres extends React.Component {
  renderLabel (genre) {
    let label = genre.prefLabel
    if (genre.genreSubdivision) {
      label += ` (${genre.genreSubdivision})`
    }
    return label
  }

  searchLink (genre) {
    return `/search?query=genre%3A"${genre}"`
  }

  render () {
    if (this.props.genres.length > 0) {
      const genres = this.props.genres.map(genre => this.renderLabel(genre))
      return (
        <aside className="work-genres">
          <h2><FormattedMessage {...messages.genre} /></h2>
          <ul data-automation-id="work_genres">
            {genres.map(genre => <li key={genre}><Link to={this.searchLink(genre)}>{genre}</Link></li>)}
          </ul>
          <a className="patron-placeholder" href="#" alt="More genres">Se flere sjangre</a>
        </aside>
      )
    } else {
      return null
    }
  }
}

Genres.defaultProps = {
  genres: []
}

Genres.propTypes = {
  genres: PropTypes.array.isRequired
}

export const messages = defineMessages({
  genre: {
    id: 'Genres.genre',
    description: 'The text displayed to identify genres',
    defaultMessage: 'Genre:'
  }
})

export default Genres
