// filebot -script fn:suball <options> <folder>


def creationDateLimit = tryQuietly{ now.time - (maxAgeDays.toLong() * 24 * 60 * 60 * 1000) }
def minFileSize = tryQuietly{ minFileSize.toLong() }; if (minFileSize == null) { minFileSize = 50 * 1000L * 1000L }
def minLengthMS = tryQuietly{ minLengthMS.toLong() }; if (minLengthMS == null) { minLengthMS = 10 * 60 * 1000L }
def ignore = tryQuietly{ ignore } ?: null


def accept = { f ->
	// ignore files that match the give ignore pattern
	if (f.path =~ ignore)
		return false
		
	// ignore files that are too old
	if (creationDateLimit != null && f.creationDate < creationDateLimit)
		return false
	
	// ignore files that are too small	
	if (minFileSize > 0 && f.length() < minFileSize)
		return false
	
	// ignore files that are too short
	if ((minLengthMS > 0 && tryQuietly{ getMediaInfo(file:f, format:'{duration}').toLong() < minLengthMS }))
		return false
	
	// ignore files that already have subtitles
	if (getMediaInfo(file:f, format:'''{media.TextCodecList}''').length() > 0)
		return false
	
	return true
}


/*
 * Get subtitles for all your media files  
 */
args.eachMediaFolder {
	def input = it.listFiles{ it.isVideo() }
	def selected = input.findAll{ accept(it) }
	
	// print excludes
	(input - selected).each{ println it	}
	
	if (selected.size() > 0) {
		getMissingSubtitles(file: selected)
	}
}
