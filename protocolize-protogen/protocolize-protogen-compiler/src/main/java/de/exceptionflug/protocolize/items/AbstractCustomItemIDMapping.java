package de.exceptionflug.protocolize.items;

public abstract class AbstractCustomItemIDMapping extends ItemIDMapping {

    public AbstractCustomItemIDMapping(final int protocolVersionRangeStart, final int protocolVersionRangeEnd, final int id) {
        super(protocolVersionRangeStart, protocolVersionRangeEnd, id);
    }


}
