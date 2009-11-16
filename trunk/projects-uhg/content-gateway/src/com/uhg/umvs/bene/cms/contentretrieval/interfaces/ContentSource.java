public interface ContentSource
{

    public boolean hasContent(String contentItem, HttpServletRequest request);

    public void getContent(String contentItem, HttpServletRequest req, HttpServletResponse resp);
}