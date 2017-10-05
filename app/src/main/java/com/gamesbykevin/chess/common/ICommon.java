package com.gamesbykevin.chess.common;

import com.gamesbykevin.androidframeworkv2.base.Disposable;

public interface ICommon extends Disposable
{
	/**
	 * Update the entity
	 */
	public void update();

	/**
	 * Logic to reset
	 */
	public void reset();

	/**
	 * Logic to render
	 * @param m Our open gl float array matrices
	 */
	public void render(float[] m);
}