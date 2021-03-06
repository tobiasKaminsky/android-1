/*
 * Nextcloud Android client application
 *
 * @author Mario Danic
 * Copyright (C) 2017 Mario Danic
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.owncloud.android.ui.events;

import com.owncloud.android.lib.resources.files.SearchRemoteOperation;

import org.parceler.Parcel;

/**
 * Search event
 */
@Parcel
public class SearchEvent {
    public String searchQuery;
    public SearchRemoteOperation.SearchType searchType;

    public SearchEvent(String searchQuery, SearchRemoteOperation.SearchType searchType) {
        this.searchQuery = searchQuery;
        this.searchType = searchType;
    }

    public SearchEvent() {
    }

    public String getSearchQuery() {
        return this.searchQuery;
    }

    public SearchRemoteOperation.SearchType getSearchType() {
        return this.searchType;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public void setSearchType(SearchRemoteOperation.SearchType searchType) {
        this.searchType = searchType;
    }
}
