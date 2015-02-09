package clearvolume.renderer.jogl.overlay.o2d;

import static java.lang.Math.random;
import gnu.trove.list.linked.TDoubleLinkedList;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import javax.media.opengl.GL4;

import cleargl.ClearGeometryObject;
import cleargl.GLFloatArray;
import cleargl.GLIntArray;
import cleargl.GLMatrix;
import cleargl.GLProgram;
import clearvolume.renderer.DisplayRequestInterface;
import clearvolume.renderer.jogl.overlay.Overlay2D;
import clearvolume.renderer.jogl.overlay.OverlayBase;

public class GraphOverlay extends OverlayBase implements Overlay2D
{
	private static final int cMaxNumberOfPoints = 512;
	private static final int cMaximalWaitTimeForDrawingInMilliseconds = 10;
	private static final float cLineWidth = 2.f; // only cLineWidth = 1.f

	// seems to be supported

	private GLProgram mGLProgram;
	private ClearGeometryObject mClearGeometryObject;

	private volatile FloatBuffer mGraphColor = FloatBuffer.wrap(new float[]
	{ 1.f, 1.f, 1.f, 1f });

	private TDoubleLinkedList mDataY = new TDoubleLinkedList();

	private final ReentrantLock mReentrantLock = new ReentrantLock();

	private volatile int mMaxCapacity = cMaxNumberOfPoints;

	private int mMaxNumberOfPoints;

	private DisplayRequestInterface mDisplayRequestInterface;
	private volatile boolean mHasChanged = false;

	private GLFloatArray mVerticesFloatArray;
	private GLIntArray mIndexIntArray;
	private GLFloatArray mNormalArray;
	private GLFloatArray mTexCoordFloatArray;



	public GraphOverlay()
	{
		super();
		for (int i = 0; i < cMaxNumberOfPoints; i++)
			addPoint(random());
	}

	@Override
	public String getName()
	{
		return "graph";
	}

	@Override
	public boolean hasChanged2D()
	{
		return mHasChanged;
	}

	public void setColor(double pR, double pG, double pB, double pA)
	{
		mGraphColor = FloatBuffer.wrap(new float[]
		{ (float) pR, (float) pG, (float) pB, (float) pA });
	}

	public int getMaxCapacity()
	{
		return mMaxCapacity;
	}

	public void setMaxCapacity(int pMaxCapacity)
	{
		mMaxCapacity = pMaxCapacity;
	}

	public void addPoint(double pY)
	{
		mReentrantLock.lock();
		try
		{
			mDataY.add(pY);
			if (mDataY.size() > getMaxCapacity())
				mDataY.removeAt(0);
			mHasChanged = true;
		}
		finally
		{
			mReentrantLock.unlock();
		}

		if (mDisplayRequestInterface != null)
			mDisplayRequestInterface.requestDisplay();
	}

	@Override
	public void init(	GL4 pGL4,
										DisplayRequestInterface pDisplayRequestInterface)
	{
		mDisplayRequestInterface = pDisplayRequestInterface;
		// box display: construct the program and related objects
		mReentrantLock.lock();
		try
		{
			mGLProgram = GLProgram.buildProgram(pGL4,
																					GraphOverlay.class,
																					"shaders/graph_vert.glsl",
																					"shaders/graph_frag.glsl");

			mClearGeometryObject = new ClearGeometryObject(	mGLProgram,
																											3,
																											GL4.GL_LINE_STRIP);
			mClearGeometryObject.setDynamic(true);

			mMaxNumberOfPoints = cMaxNumberOfPoints;

			mVerticesFloatArray = new GLFloatArray(mMaxNumberOfPoints, 3);
			mNormalArray = new GLFloatArray(mMaxNumberOfPoints, 3);
			mIndexIntArray = new GLIntArray(mMaxNumberOfPoints, 1);
			mTexCoordFloatArray = new GLFloatArray(mMaxNumberOfPoints, 2);

			mVerticesFloatArray.fillZeros();
			mNormalArray.fillZeros();
			mIndexIntArray.fillZeros();
			mTexCoordFloatArray.fillZeros();

			mClearGeometryObject.setVerticesAndCreateBuffer(mVerticesFloatArray.getFloatBuffer());
			mClearGeometryObject.setNormalsAndCreateBuffer(mNormalArray.getFloatBuffer());
			mClearGeometryObject.setTextureCoordsAndCreateBuffer(mTexCoordFloatArray.getFloatBuffer());
			mClearGeometryObject.setIndicesAndCreateBuffer(mIndexIntArray.getIntBuffer());

		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			mReentrantLock.unlock();
		}
	}

	@Override
	public void render2D(	GL4 pGL4,
												GLMatrix pProjectionMatrix,
												GLMatrix pInvVolumeMatrix)
	{

		if (isDisplayed())
		{
			try
			{
				boolean lIsLocked = mReentrantLock.tryLock(	cMaximalWaitTimeForDrawingInMilliseconds,
																										TimeUnit.MILLISECONDS);

				if (lIsLocked)
				{
					float lXOffset = -1, lYOffset = 0;

					float x = lXOffset, y = 0;
					float lStepX = 1f / mDataY.size();
					System.out.format("________________________________________\n");
					System.out.println(mDataY.size());

					mIndexIntArray.rewind();
					mVerticesFloatArray.rewind();

					for (int i = 0; i < mDataY.size(); i++)
					{
						y = (float) (lYOffset + mDataY.get(i));
						mVerticesFloatArray.add(x, y, -10);
						mIndexIntArray.add(i);
						System.out.format("%g\t%g\n", x, y);
						x += lStepX;
						// System.out.println(y);
					}
					mVerticesFloatArray.padZeros();
					mIndexIntArray.padZeros();

					mClearGeometryObject.updateVertices(mVerticesFloatArray.getFloatBuffer());
					mClearGeometryObject.updateIndices(mIndexIntArray.getIntBuffer());

					mGLProgram.use(pGL4);
					mClearGeometryObject.setProjection(pProjectionMatrix);

					pGL4.glEnable(GL4.GL_DEPTH_TEST);
					pGL4.glDepthMask(false);
					pGL4.glEnable(GL4.GL_CULL_FACE);
					pGL4.glEnable(GL4.GL_BLEND);
					pGL4.glBlendFunc(GL4.GL_ONE, GL4.GL_ONE);
					pGL4.glBlendEquation(GL4.GL_MAX);
					pGL4.glFrontFace(GL4.GL_CW);

					System.out.println("before draw");
					mClearGeometryObject.draw(); // 0, mDataY.size()
					System.out.println("after draw");

					pGL4.glDisable(GL4.GL_DEPTH_TEST);
					pGL4.glDepthMask(true);
					pGL4.glDisable(GL4.GL_CULL_FACE);



					mHasChanged = false;
				}

			}
			catch (InterruptedException e)
			{
			}
			finally
			{
				mReentrantLock.unlock();
			}
		}
	}

}
