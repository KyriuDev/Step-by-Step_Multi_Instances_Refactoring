package bpmn.types.process;

import constants.FlowDirection;
import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.ConstantRealDistribution;

import java.util.ArrayList;
import java.util.Map;

public class SequenceFlow extends BpmnProcessObject
{
    private String sourceRef;
    private String targetRef;

    private boolean probabilized;
    private AbstractRealDistribution duration;

    public SequenceFlow(String id,
                        String sourceRef,
                        String targetRef)
    {
        super(BpmnProcessType.SEQUENCE_FLOW, id);
        this.sourceRef = sourceRef;
        this.targetRef = targetRef;
        this.probabilized = false;
        this.duration = new ConstantRealDistribution(0); //Default flow
    }

    public SequenceFlow(String id,
                        String sourceRef)
    {
        super(BpmnProcessType.SEQUENCE_FLOW, id);
        this.sourceRef = sourceRef;
        this.targetRef = "";
        this.probabilized = false;
    }

    public SequenceFlow(String id)
    {
        super(BpmnProcessType.SEQUENCE_FLOW, id);
        this.sourceRef = "";
        this.targetRef = "";
        this.probabilized = false;
    }

    public void setDuration(final AbstractRealDistribution duration)
    {
        this.duration = duration;
    }

    public AbstractRealDistribution duration()
    {
        return this.duration;
    }

    public String sourceRef()
    {
        return this.sourceRef;
    }

    public String targetRef()
    {
        return this.targetRef;
    }

    public void setSourceRef(final String id)
    {
        this.sourceRef = id;
    }

    public void setTargetRef(final String id)
    {
        this.targetRef = id;
    }

    public String id()
    {
        return this.id;
    }

    public void setProbabilized()
    {
        this.probabilized = true;
    }

    public boolean probabilized()
    {
        return this.probabilized;
    }

    @Override
    public BpmnProcessObject copy()
    {
        final SequenceFlow copiedFlow = (SequenceFlow) BpmnProcessFactory.generateSequenceFlow("", "");
        copiedFlow.setDuration(this.duration);
        copiedFlow.setProbability(this.probability);

        if (this.probabilized()) copiedFlow.setProbabilized();

        return copiedFlow;
        //throw new UnsupportedOperationException("Sequence flows should never be duplicated.");
    }

    @Override
    public String toString()
    {
        return String.format("Sequence flow \"%s\" goes from source \"%s\" to destination \"%s\" with probability %f\n",
                id,
                sourceRef,
                targetRef,
                this.probability
        );
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof SequenceFlow)
        {
            return ((SequenceFlow) o).id.equals(this.id);
        }
        else if (o instanceof String)
        {
            return o.equals(this.id);
        }

        return false;
    }

    @Override
    public Map<SequenceFlow, FlowDirection> flows()
    {
        throw new UnsupportedOperationException("A SequenceFlow cannot have flows!");
    }

    @Override
    public void addFlow(SequenceFlow flow,
                        FlowDirection direction)
    {
        throw new UnsupportedOperationException("A SequenceFlow cannot have flows!");
    }

    @Override
    public void addFlow(String flow,
                        FlowDirection direction)
    {
        throw new UnsupportedOperationException("A SequenceFlow cannot have flows!");
    }

    @Override
    public void setProperFlows(ArrayList<BpmnProcessObject> objects)
    {
        throw new UnsupportedOperationException("A SequenceFlow cannot have flows!");
    }
}
