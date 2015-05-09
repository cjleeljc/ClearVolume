package clearvolume.renderer.cleargl.overlay.o2d;

import java.awt.Font;

import javax.media.opengl.GL;

import cleargl.ClearTextRenderer;
import cleargl.GLMatrix;
import clearvolume.renderer.DisplayRequestInterface;
import clearvolume.renderer.SingleKeyToggable;
import clearvolume.renderer.cleargl.overlay.OverlayForProcessors;
import clearvolume.renderer.processors.Processor;
import clearvolume.renderer.processors.ProcessorResultListener;
import clearvolume.renderer.processors.impl.OpenCLTenengrad;

import com.jogamp.newt.event.KeyEvent;

public class ImageQualityOverlay extends OverlayForProcessors	implements
																															SingleKeyToggable
{

	private final OpenCLTenengrad mOpenCLTenengrad;

	private Font mFont;
	private ClearTextRenderer mClearTextRenderer;
	private volatile double mMeasure;

	public ImageQualityOverlay()
	{
		this(20);
	}

	@SuppressWarnings("unchecked")
	public ImageQualityOverlay(int pNumberOfPointsInGraph)
	{
		super(new GraphOverlay(pNumberOfPointsInGraph));

		mOpenCLTenengrad = new OpenCLTenengrad();

		mOpenCLTenengrad.addResultListener((ProcessorResultListener<Double>) getDelegatedOverlay());

		mOpenCLTenengrad.addResultListener(new ProcessorResultListener<Double>()
		{

			@Override
			public void notifyResult(	Processor<Double> pSource,
																Double pResult)
			{
				mMeasure = pResult;
			}
		});

		addProcessor(mOpenCLTenengrad);
	}

	@Override
	public boolean toggle()
	{
		final boolean lToggleDisplay = super.toggle();

		if (lToggleDisplay)
			getGraphOverlay().clear();
		return lToggleDisplay;
	}

	public GraphOverlay getGraphOverlay()
	{
		return (GraphOverlay) getDelegatedOverlay();
	}

	@Override
	public short toggleKeyCode()
	{
		return KeyEvent.VK_I;
	}

	@Override
	public int toggleKeyModifierMask()
	{
		return 0; // KeyEvent.CTRL_MASK;
	}

	@Override
	public void init(	GL pGL,
										DisplayRequestInterface pDisplayRequestInterface)
	{
		super.init(pGL, pDisplayRequestInterface);
		final String lFontPath = "/clearvolume/fonts/SourceCodeProLight.ttf";
		try
		{

			mFont = Font.createFont(Font.TRUETYPE_FONT,
															getClass().getResourceAsStream(lFontPath))
									.deriveFont(24.f);
		}
		catch (final Throwable e)
		{
			// use a fallback font in case the original couldn't be found or
			// there has
			// been a problem
			// with the font format
			System.err.println("Could not use \"" + lFontPath
													+ "\" ("
													+ e.toString()
													+ "), falling back to Sans.");
			mFont = new Font("Sans", Font.PLAIN, 24);
		}

		mClearTextRenderer = new ClearTextRenderer(pGL, true);
	}

	@Override
	public void render2D(	GL pGL,
												int pWidth,
												int pHeight,
												GLMatrix pProjectionMatrix)
	{
		super.render2D(pGL, pWidth, pHeight, pProjectionMatrix);

		/*
		 * mClearTextRenderer.drawTextAtPosition(String.format(
		 * "image quality metric: %g", mMeasure), 10, 15, mFont,
		 * FloatBuffer.wrap(new float[] { 1.0f, 1.0f, 1.0f }), true);/*
		 */

	}

}
