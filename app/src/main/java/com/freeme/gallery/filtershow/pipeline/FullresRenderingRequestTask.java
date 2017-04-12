package com.freeme.gallery.filtershow.pipeline;

import android.graphics.Bitmap;

import com.freeme.gallery.filtershow.filters.FiltersManager;

public class FullresRenderingRequestTask extends ProcessingTask {

    private CachingPipeline mFullresPipeline = null;
    private boolean         mPipelineIsOn    = false;

    public FullresRenderingRequestTask() {
        mFullresPipeline = new CachingPipeline(
                FiltersManager.getHighresManager(), "Fullres");
    }

    public void setPreviewScaleFactor(float previewScale) {
        mFullresPipeline.setPreviewScaleFactor(previewScale);
    }

    public void setOriginal(Bitmap bitmap) {
        mFullresPipeline.setOriginal(bitmap);
        mPipelineIsOn = true;
    }

    public void stop() {
        mFullresPipeline.stop();
    }

    public void postRenderingRequest(RenderingRequest request) {
        if (!mPipelineIsOn) {
            return;
        }
        Render render = new Render();
        render.request = request;
        postRequest(render);
    }

    @Override
    public boolean isDelayedTask() {
        return true;
    }

    @Override
    public Result doInBackground(Request message) {
        RenderingRequest request = ((Render) message).request;
        RenderResult result = null;
        mFullresPipeline.render(request);
        result = new RenderResult();
        result.request = request;
        return result;
    }

    @Override
    public void onResult(Result message) {
        if (message == null) {
            return;
        }
        RenderingRequest request = ((RenderResult) message).request;
        request.markAvailable();
    }

    static class Render implements Request {
        RenderingRequest request;
    }

    static class RenderResult implements Result {
        RenderingRequest request;
    }
}
