const title = true
module.exports = {
  queryFieldTranslations: {
    'ag': 'ageLimit',
    'aktør': 'agents',
    'aldersgrense': 'ageLimit',
    'alf': 'writingSystem',
    'alfabet': 'writingSystem',
    'arr': 'musicalArranger',
    'arrangør': 'musicalArranger',
    'be': 'inst',
    'bearb': 'adaptor',
    'bearbeider': 'adaptor',
    'besetning': 'inst',
    'bid': 'contributor',
    'bidragsyter': 'contributor',
    'biografi': 'bio',
    'biography': 'bio',
    'delnummer': {translation: 'partNumber', title},
    'deltittel': 'partTitle',
    'dir': 'conductor',
    'dirigent': 'conductor',
    'dn': {translation: 'partNumber', title},
    'dt': {translation: 'partTitle', title},
    'em': 'subject',
    'emne': 'subject',
    'f': 'format',
    'fo': 'author',
    'forfatter': 'author',
    'forl': 'publishedBy',
    'forlag': 'publishedBy',
    'form': 'litform',
    'foto': 'photographer',
    'fotograf': 'photographer',
    'ge': 'genre',
    'hovedtittel': {translation: 'mainTitle', title},
    'ht': {translation: 'mainTitle', title},
    'ill': 'illustrator',
    'illustratør': 'illustrator',
    'innl': 'reader',
    'innleser': 'reader',
    'kl': 'dewey',
    'klasse': 'dewey',
    'komp': 'composer',
    'komponist': 'composer',
    'komposisjonstype': 'compType',
    'kor': 'coreographer',
    'koreograf': 'coreographer',
    'kt': 'compType',
    'land': 'country',
    'lf': 'litform',
    'manus': 'scriptWriter',
    'manusforfatter': 'scriptWriter',
    'medietype': 'mt',
    'nasj': 'nationality',
    'nasjonalitet': 'nationality',
    'odn': {translation: 'workPartNumber', title},
    'odt': {translation: 'workPartTitle', title},
    'oht': {translation: 'workMainTitle', title},
    'originalDelnummer': 'workPartNumber',
    'originalDeltittel': 'workPartTitle',
    'originalHovedtittel': 'workMainTitle',
    'originalSpråk': 'origLang',
    'originalUndertittel': 'workSubtitle',
    'originalÅr': 'firstPublicationYear',
    'ospr': 'origLang',
    'out': {translation: 'workSubtitle', title},
    'ov': 'translator',
    'oversetter': 'translator',
    'oår': 'firstPublicationYear',
    'person': 'agents',
    'prod': 'producer',
    'produsent': 'producer',
    'red': 'editor',
    'redaktør': 'editor',
    'reg': 'director',
    'regissør': 'director',
    'se': 'series',
    'serie': 'series',
    'sjanger': 'genre',
    'sku': 'actor',
    'skuespiller': 'actor',
    'spr': 'language',
    'språk': 'language',
    'tekst': 'subtitles',
    'tekstforfatter': 'lyricist',
    'tfo': 'lyricist',
    'ti': {translation: 'title', title},
    'tilp': 'adaptation',
    'tilpasning': 'adaptation',
    'tittel': {translation: 'title', title},
    'tittelnummer': 'recordId',
    'tnr': 'recordId',
    'undertekster': 'subtitles',
    'undertittel': {translation: 'subtitle', title},
    'ut': {translation: 'subtitle', title},
    'utg': 'publisher',
    'utgivelsesår': 'publicationYear',
    'utgiver': 'publisher',
    'utøv': 'performer',
    'utøver': 'performer',
    'år': 'publicationYear'
  },
  backendUri: '/services',
  maxVisibleFilterItems: 3,
  maxSearchResultsPerPage: 10,
  filterableFields: {
    branch: {
      name: 'branches',
      prefix: '',
      domain: 'publication'
    },
    mediatype: {
      name: 'mediatype',
      prefix: 'http://data.deichman.no/mediaType#',
      domain: 'publication'
    },
    format: {
      name: 'formats',
      prefix: 'http://data.deichman.no/format#',
      domain: 'publication'
    },
    language: {
      name: 'languages',
      prefix: 'http://lexvo.org/id/iso639-3/',
      domain: 'publication'
    },
    audience: {
      name: 'audiences',
      prefix: 'http://data.deichman.no/audience#',
      domain: 'work'
    },
    fictionNonfiction: {
      name: 'fictionNonfiction',
      prefix: 'http://data.deichman.no/fictionNonfiction#',
      domain: 'work'
    }
  },
  mediaTypeIcons: {
    'http://data.deichman.no/mediaType#Book': 'book',
    'http://data.deichman.no/mediaType#Audiobook': 'audiobook',
    'http://data.deichman.no/mediaType#LanguageCourse': 'book',
    'http://data.deichman.no/mediaType#MusicRecording': 'music',
    'http://data.deichman.no/mediaType#SheetMusic': 'music-note',
    'http://data.deichman.no/mediaType#Film': 'movie',
    'http://data.deichman.no/mediaType#Game': 'play',
    'http://data.deichman.no/mediaType#ComicBook': 'book',
    'http://data.deichman.no/mediaType#Map': 'book',
    'http://data.deichman.no/mediaType#Periodical': 'book',
    '': 'undefined'
  },
  mediaTypeIconsMap: {
    'book': 'icon-book',
    'audiobook': 'icon-audiobook',
    'music': 'icon-music',
    'music-note': 'icon-music',
    'movie': 'icon-movie',
    'play': 'icon-cd',
    'undefined': 'icon-help'
  },
  preferredLanguages: [
    'http://lexvo.org/id/iso639-3/nob',
    'http://lexvo.org/id/iso639-3/nno',
    'http://lexvo.org/id/iso639-3/nor',
    'http://lexvo.org/id/iso639-3/eng',
    'http://lexvo.org/id/iso639-3/swe',
    'http://lexvo.org/id/iso639-3/dan',
    'http://lexvo.org/id/iso639-3/ger',
    'http://lexvo.org/id/iso639-3/fre',
    'http://lexvo.org/id/iso639-3/spa',
    'http://lexvo.org/id/iso639-3/ita'
  ],
  enabledParameter: null /* used to clarify url parameters without value, e.g. ?hideFilters */
}
