package clearvolume.renderer.processors;

import java.util.ArrayList;

import clearvolume.renderer.SingleKeyToggable;

import com.jogamp.newt.event.KeyEvent;

public abstract class ProcessorBase<R>	implements
																				Processor<R>,
																				SingleKeyToggable
{
	private final ArrayList<ProcessorResultListener<R>> mListenerList = new ArrayList<ProcessorResultListener<R>>();

	private volatile boolean mActive = true;

	/* (non-Javadoc)
	 * @see clearvolume.renderer.processors.Processor#setActive(boolean)
	 */
	@Override
	public void setActive(boolean pIsActive)
	{
		mActive = pIsActive;
	}

	/* (non-Javadoc)
	 * @see clearvolume.renderer.cleargl.overlay.Overlay#toggleDisplay()
	 */
	@Override
	public boolean toggle()
	{
		setActive(!isActive());
		return isActive();
	}

	/* (non-Javadoc)
	 * @see clearvolume.renderer.processors.Processor#isActive()
	 */
	@Override
	public boolean isActive()
	{
		return mActive;
	}

	@Override
	public short toggleKeyCode()
	{
		return KeyEvent.VK_UNDEFINED;
	};

	@Override
	public int toggleKeyModifierMask()
	{
		return 0;
	};

	@Override
	public void addResultListener(ProcessorResultListener<R> pProcessorResultListener)
	{
		mListenerList.add(pProcessorResultListener);
	};

	@Override
	public void removeResultListener(ProcessorResultListener<R> pProcessorResultListener)
	{
		mListenerList.remove(pProcessorResultListener);
	};

	public void notifyListenersOfResult(R pResult)

	{
		for (final ProcessorResultListener<R> lListener : mListenerList)
			lListener.notifyResult(this, pResult);
	};

	@Override
	public abstract boolean isCompatibleProcessor(Class<?> pRendererClass);

	@Override
	public abstract void process(	int pRenderLayerIndex,
																long pWidthInVoxels,
																long pHeightInVoxels,
																long pDepthInVoxels);

	/**
	 * Integral division, rounding the result to the next highest integer.
	 *
	 * @param a
	 *          Dividend
	 * @param b
	 *          Divisor
	 * @return a/b rounded to the next highest integer.
	 */
	protected static long iDivUp(final long a, final long b)
	{
		return ((a % b != 0) ? (a / b + 1) : (a / b));
	}
}
