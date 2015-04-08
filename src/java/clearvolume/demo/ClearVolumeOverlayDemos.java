package clearvolume.demo;

import static java.lang.Math.random;

import java.io.IOException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

import org.junit.Test;

import clearvolume.renderer.ClearVolumeRendererInterface;
import clearvolume.renderer.cleargl.overlay.o2d.GraphOverlay;
import clearvolume.renderer.cleargl.overlay.o3d.DriftOverlay;
import clearvolume.renderer.cleargl.overlay.o3d.PathOverlay;
import clearvolume.renderer.factory.ClearVolumeRendererFactory;
import clearvolume.transferf.TransferFunctions;
import coremem.types.NativeTypeEnum;

public class ClearVolumeOverlayDemos
{

	public static void main(final String[] argv) throws ClassNotFoundException
	{
		if (argv.length == 0)
		{
			final Class<?> c = Class.forName("clearvolume.demo.ClearVolumeDemo");

			System.out.println("Give one of the following method names as parameter:");

			for (final Member m : c.getMethods())
			{
				final String name = ((Method) m).getName();

				if (name.substring(0, 4).equals("demo"))
				{
					System.out.println("Demo: " + ((Method) m).getName());
				}
			}
		}
		else
		{
			final ClearVolumeOverlayDemos cvdemo = new ClearVolumeOverlayDemos();
			Method m;

			try
			{
				m = cvdemo.getClass().getMethod(argv[0]);
			}
			catch (final Exception e)
			{
				System.out.println("Could not launch " + argv[0]
														+ " because ...");
				e.printStackTrace();

				return;
			}

			try
			{
				System.out.println("Running " + argv[0] + "()...");
				m.invoke(cvdemo);
			}
			catch (final Exception e)
			{
				e.printStackTrace();
			}
		}

	}


	@Test
	public void demoDriftOverlay() throws InterruptedException,
																IOException
	{

		final ClearVolumeRendererInterface lClearVolumeRenderer = ClearVolumeRendererFactory.newOpenCLRenderer(	"ClearVolumeTest",
																																																						512,
																																																						512,
																																																						NativeTypeEnum.UnsignedByte,
																																																						512,
																																																						512,
																																																						1,
																																																						false);
		lClearVolumeRenderer.addOverlay(new PathOverlay());

		final RandomWalk RandomWalk = new RandomWalk();
		final DriftOverlay driftOverlay = new DriftOverlay();
		lClearVolumeRenderer.addOverlay(driftOverlay);
		RandomWalk.addResultListener(driftOverlay);

		lClearVolumeRenderer.addProcessor(RandomWalk);

		lClearVolumeRenderer.setTransferFunction(TransferFunctions.getGrayLevel());
		lClearVolumeRenderer.setVisible(true);

		final int lResolutionX = 256;
		final int lResolutionY = lResolutionX;
		final int lResolutionZ = lResolutionX;

		final byte[] lVolumeDataArray = new byte[lResolutionX * lResolutionY
																							* lResolutionZ];

		for (int z = 0; z < lResolutionZ; z++)
			for (int y = 0; y < lResolutionY; y++)
				for (int x = 0; x < lResolutionX; x++)
				{
					final int lIndex = x + lResolutionX
															* y
															+ lResolutionX
															* lResolutionY
															* z;
					int lCharValue = (((byte) x ^ (byte) y ^ (byte) z));
					if (lCharValue < 12)
						lCharValue = 0;

					// mVolumeDataArray[lIndex] = (byte) lCharValue;
					lVolumeDataArray[lIndex] = (byte) (255 * x * x

					/ lResolutionX / lResolutionX);

				}

		lClearVolumeRenderer.setVolumeDataBuffer(	0,
																							ByteBuffer.wrap(lVolumeDataArray),
																							lResolutionX,
																							lResolutionY,
																							lResolutionZ);
		lClearVolumeRenderer.requestDisplay();

		while (lClearVolumeRenderer.isShowing())
		{
			Thread.sleep(1000);
			lClearVolumeRenderer.setVolumeDataBuffer(	0,
																								ByteBuffer.wrap(lVolumeDataArray),
																								lResolutionX,
																								lResolutionY,
																								lResolutionZ);
			lClearVolumeRenderer.requestDisplay();
		}

		lClearVolumeRenderer.close();
	}


	@Test
	public void demoGraphOverlay() throws InterruptedException,
																IOException
	{

		final ClearVolumeRendererInterface lClearVolumeRenderer = ClearVolumeRendererFactory.newBestRenderer(	"ClearVolumeTest",
																																																					512,
																																																					512,
																																																					NativeTypeEnum.UnsignedByte,
																																																					512,
																																																					512,
																																																					1,
																																																					false);

		final GraphOverlay lGraphOverlay = new GraphOverlay(256);
		lClearVolumeRenderer.addOverlay(lGraphOverlay);

		lClearVolumeRenderer.setTransferFunction(TransferFunctions.getGrayLevel());
		lClearVolumeRenderer.setVisible(true);

		final int lResolutionX = 256;
		final int lResolutionY = lResolutionX;
		final int lResolutionZ = lResolutionX;

		final byte[] lVolumeDataArray = new byte[lResolutionX * lResolutionY
																							* lResolutionZ];

		for (int z = 0; z < lResolutionZ; z++)
			for (int y = 0; y < lResolutionY; y++)
				for (int x = 0; x < lResolutionX; x++)
				{
					final int lIndex = x + lResolutionX
															* y
															+ lResolutionX
															* lResolutionY
															* z;
					int lCharValue = (((byte) x ^ (byte) y ^ (byte) z));
					if (lCharValue < 12)
						lCharValue = 0;
					lVolumeDataArray[lIndex] = (byte) lCharValue;
				}

		lClearVolumeRenderer.setVolumeDataBuffer(	0,
																							ByteBuffer.wrap(lVolumeDataArray),
																							lResolutionX,
																							lResolutionY,
																							lResolutionZ);
		lClearVolumeRenderer.requestDisplay();

		double lTrend = 0;
		while (lClearVolumeRenderer.isShowing())
		{
			Thread.sleep(500);
			lTrend += 0.05 * (Math.random() - 0.5);
			final double lValue = lTrend + 0.02 * Math.random();
			lGraphOverlay.addPoint(lValue);
		}

		lClearVolumeRenderer.close();
	}

	@Test
	public void demoPathOverlay3D()	throws InterruptedException,
																	IOException
	{
		final ClearVolumeRendererInterface lClearVolumeRenderer = ClearVolumeRendererFactory.newBestRenderer(	"ClearVolumeTest",
																																																					1024,
																																																					1024,
																																																					NativeTypeEnum.UnsignedByte,
																																																					512,
																																																					512,
																																																					1,
																																																					false);

		final PathOverlay lPathOverlay = new PathOverlay();
		lClearVolumeRenderer.addOverlay(lPathOverlay);

		lClearVolumeRenderer.setTransferFunction(TransferFunctions.getGrayLevel());
		lClearVolumeRenderer.setVisible(true);

		final int lResolutionX = 256;
		final int lResolutionY = lResolutionX;
		final int lResolutionZ = lResolutionX;

		final byte[] lVolumeDataArray = new byte[lResolutionX * lResolutionY
																							* lResolutionZ];

		for (int z = 0; z < lResolutionZ; z++)
			for (int y = 0; y < lResolutionY; y++)
				for (int x = 0; x < lResolutionX; x++)
				{
					final int lIndex = x + lResolutionX
															* y
															+ lResolutionX
															* lResolutionY
															* z;
					int lCharValue = (((byte) x ^ (byte) y ^ (byte) z));
					if (lCharValue < 12)
						lCharValue = 0;
					lVolumeDataArray[lIndex] = (byte) lCharValue;
				}

		lClearVolumeRenderer.setVolumeDataBuffer(	0,
																							ByteBuffer.wrap(lVolumeDataArray),
																							lResolutionX,
																							lResolutionY,
																							lResolutionZ);
		lClearVolumeRenderer.requestDisplay();

		float x = 0, y = 0, z = 0;
		while (lClearVolumeRenderer.isShowing())
		{
			Thread.sleep(500);
			lPathOverlay.addPathPoint(x, y, z);

			x += 0.01 * (random() - 0.5);
			y += 0.01 * (random() - 0.5);
			z += 0.01 * (random() - 0.5);

			lClearVolumeRenderer.requestDisplay();
		}

		lClearVolumeRenderer.close();
	}


}
