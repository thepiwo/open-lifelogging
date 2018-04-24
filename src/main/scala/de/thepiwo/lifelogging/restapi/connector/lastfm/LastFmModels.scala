package de.thepiwo.lifelogging.restapi.connector.lastfm

case class LastFmRecentTrackBase(recenttracks: LastFmRecentTracks)

case class LastFmRecentTracks(track: Seq[LastFmTrack],
                              `@attr`: LastFmAttr)

case class LastFmAttr(user: String,
                      page: String,
                      perPage: String,
                      totalPages: String,
                      total: String)

case class LastFmTrack(artist: LastFmArtist,
                       name: String,
                       mbid: String,
                       album: LastFmAlbum,
                       date: Option[LastFmDate])

case class LastFmArtist(`#text`: String,
                        mbid: String)

case class LastFmAlbum(`#text`: String,
                       mbid: String)

case class LastFmDate(uts: String)