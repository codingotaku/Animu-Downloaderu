package com.codingotaku.apps.exception;

import java.util.NoSuchElementException;

import com.codingotaku.apps.source.Anime;

public class NoSuchListException extends NoSuchElementException {

	public NoSuchListException(Anime anime, int page) {
		super(String.format("Anime %s doesn't have %d pages", anime.getName(), page));
	}

	public NoSuchListException(int page) {
		super(String.format("Selected server doesn't have %d pages", page));
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
